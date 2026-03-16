package org.hnau.pinfin.projector.transaction.delegates

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.hnau.commons.app.projector.uikit.AlertDialogContent
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.model.transaction.TransactionModel
import org.hnau.pinfin.projector.Localization

class DialogsProjector(
    private val model: TransactionModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }


    @Composable
    fun Content() {
        Cancel()
        Remove()
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
                        title = { Text((dependencies.localization.saveChanges)) },
                        buttons = {
                            TextButton(
                                onClick = info.cancelChanges,
                                content = { Text((dependencies.localization.notSave)) },
                            )
                            info.saveIfPossible.foldNullable(
                                ifNull = {
                                    TextButton(
                                        onClick = info.close,
                                        content = { Text((dependencies.localization.close)) },
                                    )
                                },
                                ifNotNull = { save ->
                                    TextButton(
                                        onClick = save,
                                        content = { Text((dependencies.localization.save)) },
                                    )
                                }
                            )
                        }
                    )
                }
            }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Remove() {
        model
            .removeDialogInfo
            .collectAsState()
            .value
            ?.let { info ->
                BasicAlertDialog(
                    onDismissRequest = info.close,
                ) {
                    AlertDialogContent(
                        title = { Text((dependencies.localization.removeTransaction)) },
                        dismissButton = {
                            TextButton(
                                onClick = info.close,
                                content = { Text((dependencies.localization.no)) },
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = info.remove,
                                content = { Text((dependencies.localization.yes)) },
                            )
                        }
                    )
                }
            }
    }
}