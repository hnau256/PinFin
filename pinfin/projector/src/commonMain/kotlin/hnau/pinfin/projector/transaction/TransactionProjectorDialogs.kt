package hnau.pinfin.projector.transaction

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import hnau.common.kotlin.foldNullable
import hnau.common.projector.uikit.AlertDialogContent
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.close
import hnau.pinfin.projector.resources.no
import hnau.pinfin.projector.resources.not_save
import hnau.pinfin.projector.resources.remove_transaction
import hnau.pinfin.projector.resources.save
import hnau.pinfin.projector.resources.save_changes
import hnau.pinfin.projector.resources.yes
import org.jetbrains.compose.resources.stringResource

@Composable
fun Dialogs(
    model: TransactionModel,
) {
    ExitUnsavedDialog(
        model = model,
    )
    RemoveDialog(
        model = model,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExitUnsavedDialog(
    model: TransactionModel,
) {
    val info = model.exitUnsavedDialogInfo.collectAsState().value ?: return
    BasicAlertDialog(
        onDismissRequest = info.dismiss,
    ) {
        AlertDialogContent(
            title = { Text(stringResource(Res.string.save_changes)) },
            buttons = {
                TextButton(
                    onClick = info.exitWithoutSaving,
                    content = { Text(stringResource(Res.string.not_save)) },
                )
                info.save.foldNullable(
                    ifNull = {
                        TextButton(
                            onClick = info.dismiss,
                            content = { Text(stringResource(Res.string.close)) },
                        )
                    },
                    ifNotNull = { save ->
                        TextButton(
                            onClick = save,
                            content = { Text(stringResource(Res.string.save)) },
                        )
                    }
                )
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemoveDialog(
    model: TransactionModel,
) {
    val info = model.removeDialogInfo.collectAsState().value ?: return
    BasicAlertDialog(
        onDismissRequest = info.dismiss,
    ) {
        AlertDialogContent(
            title = { Text(stringResource(Res.string.remove_transaction)) },
            dismissButton = {
                TextButton(
                    onClick = info.dismiss,
                    content = { Text(stringResource(Res.string.no)) },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = info.remove,
                    content = { Text(stringResource(Res.string.yes)) },
                )
            }
        )
    }
}