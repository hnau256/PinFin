package hnau.pinfin.projector.transaction

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import hnau.common.projector.uikit.HnauButton
import hnau.common.projector.uikit.TripleRow
import hnau.common.projector.uikit.table.Cell
import hnau.common.projector.uikit.table.Subtable
import hnau.common.projector.uikit.table.Table
import hnau.common.projector.uikit.table.TableOrientation
import hnau.common.projector.utils.Icon
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.cancel
import hnau.pinfin.projector.resources.save
import hnau.pipe.annotations.Pipe
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
class TransactionProjectorMainInfoTimeDelegate(
    scope: CoroutineScope,
    private val model: TransactionModel.MainContent.Time,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    private fun createCells(
        state: TimePickerState,
    ): ImmutableList<Cell> = persistentListOf(
        Cell {
            TimePicker(
                state = state,
                modifier = Modifier
                    .clip(shape),
            )
        },
        Subtable(
            cells = persistentListOf(
                Cell {
                    HnauButton(
                        modifier = Modifier.weight(1f),
                        shape = shape,
                        onClick = model.cancel,
                        content = {
                            TripleRow(
                                leading = { Icon(Icons.Filled.Clear) },
                                content = {
                                    Text(
                                        text = stringResource(Res.string.cancel)
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
                        onClick = {
                            model.save(
                                LocalTime(
                                    hour = state.hour,
                                    minute = state.minute,
                                    second = 0,
                                    nanosecond = 0,
                                )
                            )
                        },
                        content = {
                            TripleRow(
                                leading = { Icon(Icons.Filled.Done) },
                                content = {
                                    Text(
                                        text = stringResource(Res.string.save)
                                    )
                                }
                            )
                        }
                    )
                })
        )
    )

    @Composable
    fun Content() {

        val state = rememberTimePickerState(
            initialHour = model.initialTime.hour,
            initialMinute = model.initialTime.minute,
        )

        Table(
            orientation = TableOrientation.Vertical,
            cells = remember(state) { createCells(state) },
        )
    }
}