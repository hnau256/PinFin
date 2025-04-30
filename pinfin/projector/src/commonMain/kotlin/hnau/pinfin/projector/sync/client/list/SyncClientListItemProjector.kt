package hnau.pinfin.projector.sync.client.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hnau.common.compose.uikit.HnauButton
import hnau.common.compose.uikit.table.CellBox
import hnau.common.compose.uikit.table.Table
import hnau.common.compose.uikit.table.TableOrientation
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.Icon
import hnau.common.kotlin.fold
import hnau.pinfin.model.sync.client.list.SyncClientListItemModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class SyncClientListItemProjector(
    scope: CoroutineScope,
    private val model: SyncClientListItemModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies

    @Composable
    fun Content() {
        Table(
            orientation = TableOrientation.Horizontal,
        ) {
            CellBox(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(
                            horizontal = Dimens.separation,
                            vertical = Dimens.smallSeparation,
                        ),
                    text = model.id.id.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            model
                .state
                .collectAsState()
                .value
                .let { stateOrLoading ->
                    stateOrLoading.fold(
                        ifLoading = {
                            CellBox(
                                modifier = Modifier
                                    .width(buttonWidth),
                            ) {
                                CircularProgressIndicator()
                            }
                        },
                        ifReady = { state ->
                            when (state) {
                                SyncClientListItemModel.State.Actual ->
                                    CellBox(
                                        modifier = Modifier
                                            .width(buttonWidth),
                                    ) {
                                        Icon(
                                            tint = MaterialTheme.colorScheme.primary,
                                        ) {
                                            Icons.Filled.CloudDone
                                        }
                                    }

                                is SyncClientListItemModel.State.Syncable -> Cell {
                                    HnauButton(
                                        modifier = Modifier
                                            .width(buttonWidth),
                                        onClick = state.sync,
                                    ) {
                                        Icon {
                                            when (state.mode) {
                                                SyncClientListItemModel.State.Syncable.Mode.OnlyOnServer ->
                                                    Icons.Filled.CloudDownload

                                                SyncClientListItemModel.State.Syncable.Mode.OnlyLocal ->
                                                    Icons.Filled.CloudUpload

                                                SyncClientListItemModel.State.Syncable.Mode.Both ->
                                                    Icons.Filled.Sync
                                            }
                                        }
                                    }
                                }
                            }
                        },
                    )
                }
        }
    }

    companion object {

        private val buttonWidth: Dp = 64.dp
    }
}