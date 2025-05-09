package hnau.pinfin.projector.transaction

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import hnau.common.compose.uikit.ContainerStyle
import hnau.common.compose.uikit.HnauButton
import hnau.common.compose.uikit.TextInput
import hnau.common.compose.uikit.TripleRow
import hnau.common.compose.uikit.table.Subtable
import hnau.common.compose.uikit.table.Table
import hnau.common.compose.uikit.table.TableOrientation
import hnau.common.compose.uikit.table.cellShape
import hnau.common.compose.utils.Icon
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.comment
import hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import hnau.pinfin.projector.utils.title
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource


class TransactionProjectorMainInfoConfigDelegate(
    scope: CoroutineScope,
    private val model: TransactionModel.MainContent.Config,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        val dateTimeFormatter: DateTimeFormatter
    }

    @Composable
    fun Content() {
        Table(
            orientation = TableOrientation.Vertical,
        ) {
            Subtable {
                Cell {
                    HnauButton(
                        modifier = Modifier.weight(1f),
                        shape = cellShape,
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
                Cell {
                    HnauButton(
                        modifier = Modifier.weight(1f),
                        shape = cellShape,
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
            Cell {
                TextInput(
                    placeholder = { Text(stringResource(Res.string.comment)) },
                    value = model.comment,
                    shape = cellShape,
                )
            }
            Subtable {
                val selectedType by model.typeVariant.collectAsState()
                TransactionType
                    .entries
                    .fastForEach { type ->
                        Cell {
                            HnauButton(
                                shape = cellShape,
                                onClick = { model.chooseType(type) },
                                style = when (type) {
                                    selectedType -> ContainerStyle.Primary
                                    else -> ContainerStyle.Neutral
                                },
                                modifier = Modifier
                                    .weight(1f),
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