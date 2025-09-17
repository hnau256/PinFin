package hnau.pinfin.projector.sync.client.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hnau.common.app.projector.uikit.table.CellBox
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.sync.client.list.SyncClientListItemModel
import hnau.pinfin.projector.utils.BidgetInfoContent

class SyncClientListItemProjector(
    private val model: SyncClientListItemModel,
) {


    @Composable
    fun Content() {
        Table(
            orientation = TableOrientation.Horizontal,
            modifier = Modifier.fillMaxWidth(),
        ) {
            CellBox(
                configModifier = { modifier -> modifier.weight(1f) },
                isLast = false,
            ) {
                BidgetInfoContent(
                    info = model
                        .info
                        .collectAsState()
                        .value
                )
            }

            model
                .state
                .let { state ->
                    when (state) {
                        SyncClientListItemModel.State.Actual ->
                            CellBox(
                                isLast = false,
                                configModifier = { modifier -> modifier.width(buttonWidth) },
                            ) {
                                Icon(
                                    tint = MaterialTheme.colorScheme.primary,
                                    icon = Icons.Filled.CloudDone,
                                )
                            }

                        is SyncClientListItemModel.State.Syncable -> Cell(
                            isLast = true,
                        ) { modifier ->
                            Button(
                                modifier = modifier.width(buttonWidth),
                                shape = shape,
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
        }
    }

    companion object {

        private val buttonWidth: Dp = 64.dp
    }
}