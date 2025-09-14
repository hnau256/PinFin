package hnau.pinfin.projector.transaction.delegates

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import hnau.common.app.projector.uikit.AlertDialogContent
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.close
import hnau.pinfin.projector.resources.not_save
import hnau.pinfin.projector.resources.save
import hnau.pinfin.projector.resources.save_changes
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

class DialogsProjector(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val model: TransactionModel,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content() {
        Cancel()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Cancel() {
        model
            .cancelDialogInfo
            .collectAsState()
            .value
            ?.let { info ->
                BasicAlertDialog(
                    onDismissRequest = info.close,
                ) {
                    AlertDialogContent(
                        title = { Text(stringResource(Res.string.save_changes)) },
                        buttons = {
                            TextButton(
                                onClick = info.cancelChanges,
                                content = { Text(stringResource(Res.string.not_save)) },
                            )
                            info.saveIfPossible.foldNullable(
                                ifNull = {
                                    TextButton(
                                        onClick = info.close,
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
    }
}