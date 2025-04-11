package hnau.pinfin.client.projector.transaction.type.entry.record

import androidx.compose.runtime.Composable
import hnau.common.compose.uikit.ContainerStyle
import hnau.pinfin.client.model.transaction.type.entry.record.RecordModel
import hnau.pinfin.client.projector.utils.Dialog

class RecordProjectorRemoveDelegate(
    private val remove: () -> Unit,
    private val model: RecordModel,
) {

    @Composable
    fun Content() {
        //TODO("ComposeForAndroid")
        Dialog(
            title = "QWERTY",//stringResource(R.string.remove_record),
        ) {
            Button(
                text = "QWERTY",//stringResource(R.string.no),
                onClick = model::closeOverlap,
                style = ContainerStyle.Neutral,
            )
            Button(
                text = "QWERTY",//stringResource(R.string.yes),
                onClick = remove,
                style = ContainerStyle.Error,
            )
        }
    }
}