package hnau.pinfin.client.projector.transaction.type.entry.record

import androidx.compose.runtime.Composable
import hnau.common.compose.uikit.ContainerStyle
import hnau.pinfin.client.model.transaction.type.entry.record.RecordModel
import hnau.pinfin.client.projector.utils.Dialog
import org.jetbrains.compose.resources.stringResource
import pinfin.pinfin.client.projector.generated.resources.Res
import pinfin.pinfin.client.projector.generated.resources.no
import pinfin.pinfin.client.projector.generated.resources.remove_record
import pinfin.pinfin.client.projector.generated.resources.yes

class RecordProjectorRemoveDelegate(
    private val remove: () -> Unit,
    private val model: RecordModel,
) {

    @Composable
    fun Content() {
        Dialog(
            title = stringResource(Res.string.remove_record),
        ) {
            Button(
                text = stringResource(Res.string.no),
                onClick = model::closeOverlap,
                style = ContainerStyle.Neutral,
            )
            Button(
                text = stringResource(Res.string.yes),
                onClick = remove,
                style = ContainerStyle.Error,
            )
        }
    }
}