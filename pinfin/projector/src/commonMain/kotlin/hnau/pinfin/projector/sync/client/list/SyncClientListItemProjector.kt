package hnau.pinfin.projector.sync.client.list

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hnau.common.projector.uikit.HnauButton
import hnau.common.projector.uikit.table.Cell
import hnau.common.projector.uikit.table.CellBox
import hnau.common.projector.uikit.table.Table
import hnau.common.projector.uikit.table.TableOrientation
import hnau.common.projector.utils.Icon
import hnau.pinfin.model.sync.client.list.SyncClientListItemModel
import hnau.pinfin.projector.utils.BidgetInfoContent
import hnau.pipe.annotations.Pipe
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope

class SyncClientListItemProjector(
    scope: CoroutineScope,
    private val model: SyncClientListItemModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    private val cells: ImmutableList<Cell> = persistentListOf(
        CellBox(
            weight = 1f,
        ) {
            BidgetInfoContent(
                info = model
                    .info
                    .collectAsState()
                    .value
            )
        },
        model
            .state
            .let { state ->
                when (state) {
                    SyncClientListItemModel.State.Actual ->
                        CellBox(
                            modifier = Modifier
                                .width(buttonWidth),
                        ) {
                            Icon(
                                tint = MaterialTheme.colorScheme.primary,
                                icon = Icons.Filled.CloudDone,
                            )
                        }

                    is SyncClientListItemModel.State.Syncable -> Cell {
                        HnauButton(
                            modifier = Modifier
                                .width(buttonWidth),
                            onClick = state.sync,
                        ) {
                            Icon(
                                icon = when (state.mode) {
                                    SyncClientListItemModel.State.Syncable.Mode.OnlyOnServer ->
                                        Icons.Filled.CloudDownload

                                    SyncClientListItemModel.State.Syncable.Mode.OnlyLocal ->
                                        Icons.Filled.CloudUpload

                                    SyncClientListItemModel.State.Syncable.Mode.Both ->
                                        Icons.Filled.Sync
                                }
                            )
                        }
                    }
                }

            }
    )

    @Composable
    fun Content() {
        Table(
            orientation = TableOrientation.Horizontal,
            cells = cells,
        )
    }

    companion object {

        private val buttonWidth: Dp = 64.dp
    }
}