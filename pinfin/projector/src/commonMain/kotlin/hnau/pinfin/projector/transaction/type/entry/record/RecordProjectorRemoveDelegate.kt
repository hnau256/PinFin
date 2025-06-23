package hnau.pinfin.projector.transaction.type.entry.record

import androidx.compose.runtime.Composable
import hnau.common.projector.uikit.ContainerStyle
import hnau.pinfin.model.transaction.type.entry.record.RecordModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.no
import hnau.pinfin.projector.resources.remove_record
import hnau.pinfin.projector.resources.yes
import hnau.pinfin.projector.utils.Dialog
import org.jetbrains.compose.resources.stringResource

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