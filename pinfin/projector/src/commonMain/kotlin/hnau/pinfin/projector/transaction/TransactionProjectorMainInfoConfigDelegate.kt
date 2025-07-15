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
import hnau.common.app.projector.uikit.ContainerStyle
import hnau.common.app.projector.uikit.HnauButton
import hnau.common.app.projector.uikit.TextInput
import hnau.common.app.projector.uikit.TripleRow
import hnau.common.app.projector.uikit.table.Cell
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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

    private val cells: ImmutableList<Cell> = persistentListOf(
        Subtable(
            cells = persistentListOf(
                Cell {
                    HnauButton(
                        modifier = Modifier.weight(1f),
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
                },
                Cell {
                    HnauButton(
                        modifier = Modifier.weight(1f),
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
            ),
        ),
        Cell {
            TextInput(
                placeholder = { Text(stringResource(Res.string.comment)) },
                value = model.comment,
                shape = shape,
            )
        },
        Subtable(
            cells = TransactionType
                .entries
                .map { type ->
                    Cell {
                        val selectedType by model.typeVariant.collectAsState()
                        HnauButton(
                            modifier = Modifier.weight(1f),
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
                .toImmutableList()
        )
    )

    @Composable
    fun Content() {
        Table(
            orientation = TableOrientation.Vertical,
            cells = cells,
        )
    }

}