package hnau.pinfin.projector.transaction

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import hnau.common.app.projector.uikit.ContainerStyle
import hnau.common.app.projector.uikit.HnauButton
import hnau.common.app.projector.uikit.TextInput
import hnau.common.app.projector.uikit.TripleRow
import hnau.common.app.projector.uikit.table.Subtable
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.comment
import hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import hnau.pinfin.projector.utils.title
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource


class TransactionProjectorMainInfoConfigDelegate(
    scope: CoroutineScope,
    private val model: TransactionModel.MainContent.Config,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val dateTimeFormatter: DateTimeFormatter
    }

    @Composable
    fun Content() {
        Table(
            orientation = TableOrientation.Vertical,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Subtable(
                isLast = false,
            ) {
                Cell(
                    isLast = false,
                ) { modifier ->
                    HnauButton(
                        modifier = modifier.weight(1f),
                        shape = shape,
                        onClick = model.chooseDate,
                        content = {
                            TripleRow(
                                leading = { Icon(Icons.Filled.CalendarMonth) },
                                content = {
                                    Text(
                                        text = model
                                            .date
                                            .collectAsState()
                                            .value
                                            .let(dependencies.dateTimeFormatter::formatDate),
                                    )
                                }
                            )
                        }
                    )
                }
                Cell(
                    isLast = true,
                ) { modifier ->
                    HnauButton(
                        modifier = modifier.weight(1f),
                        shape = shape,
                        onClick = model.chooseTime,
                        content = {
                            TripleRow(
                                leading = { Icon(Icons.Filled.Schedule) },
                                content = {
                                    Text(
                                        text = model
                                            .time
                                            .collectAsState()
                                            .value
                                            .let(dependencies.dateTimeFormatter::formatTime),
                                    )
                                }
                            )
                        }
                    )
                }
            }
            Cell(
                isLast = false,
            ) { modifier ->
                TextInput(
                    maxLines = 1,
                    modifier = modifier,
                    placeholder = { Text(stringResource(Res.string.comment)) },
                    value = model.comment,
                    shape = shape,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    )
                )
            }
            Subtable(
                isLast = true,
            ) {
                TransactionType
                    .entries
                    .mapIndexed { i, type ->
                        Cell(
                            isLast = i == TransactionType.entries.lastIndex,
                        ) { modifier ->
                            val selectedType by model.typeVariant.collectAsState()
                            HnauButton(
                                modifier = modifier.weight(1f),
                                shape = shape,
                                onClick = { model.chooseType(type) },
                                style = when (type) {
                                    selectedType -> ContainerStyle.Primary
                                    else -> ContainerStyle.Neutral
                                },
                                content = {
                                    TripleRow(
                                        leading = when (type) {
                                            selectedType -> {
                                                {
                                                    Icon(Icons.Filled.Done)
                                                }
                                            }

                                            else -> null
                                        },
                                        content = {
                                            Text(
                                                text = type.title,
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    }
            }
        }
    }

}