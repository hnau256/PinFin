package hnau.pinfin.projector.transaction

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import hnau.common.compose.uikit.AlertDialogContent
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.close
import hnau.pinfin.projector.no
import hnau.pinfin.projector.not_save
import hnau.pinfin.projector.remove_transaction
import hnau.pinfin.projector.save
import hnau.pinfin.projector.save_changes
import hnau.pinfin.projector.yes
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
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
    DateDialog(
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDialog(
    model: TransactionModel,
) {
    val info = model.pickDatDialogInfo.collectAsState().value ?: return
    BasicAlertDialog(
        onDismissRequest = info.dismiss,
    ) {
        val modelDate = model
            .date
            .collectAsState()
            .value
        val state = rememberDatePickerState(
            initialSelectedDateMillis = modelDate
                .atStartOfDayIn(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
        )
        AlertDialogContent(
            text = {
                DatePicker(
                    state = state,
                )
            },
            dismissButton = {
                TextButton(
                    onClick = info.dismiss,
                    content = { Text(stringResource(Res.string.no)) },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = state
                            .selectedDateMillis
                            ?.let(Instant.Companion::fromEpochMilliseconds)
                            ?.toLocalDateTime(TimeZone.currentSystemDefault())
                            ?.date
                        info.save(
                            selectedDate ?: modelDate
                        )
                    },
                    content = { Text(stringResource(Res.string.yes)) },
                )
            }
        )
    }
}