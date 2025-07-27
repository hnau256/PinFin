package hnau.pinfin.projector.transaction.type.entry.record

import androidx.compose.runtime.Composable
import hnau.common.app.projector.uikit.ContainerStyle
import hnau.pinfin.model.transaction.type.entry.record.RecordModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.no
import hnau.pinfin.projector.resources.remove_record
import hnau.pinfin.projector.resources.yes
import hnau.pinfin.projector.utils.Dialog
import hnau.pinfin.projector.utils.DialogButton
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

class RecordProjectorRemoveDelegate(
    remove: () -> Unit,
    private val model: RecordModel,
) {

    private val buttons = persistentListOf(
        DialogButton(
            text = { stringResource(Res.string.no) },
            onClick = model::closeOverlap,
            style = ContainerStyle.neutral,
        ),
        DialogButton(
            text = { stringResource(Res.string.yes) },
            onClick = remove,
            style = ContainerStyle.error,
        )
    )

    @Composable
    fun Content() {
        Dialog(
            title = stringResource(Res.string.remove_record),
            buttons = buttons,
        )
    }
}