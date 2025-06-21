package hnau.pinfin.projector.transaction

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import hnau.common.projector.uikit.HnauButton
import hnau.common.projector.uikit.TripleRow
import hnau.common.projector.uikit.table.Subtable
import hnau.common.projector.uikit.table.Table
import hnau.common.projector.uikit.table.TableOrientation
import hnau.common.projector.uikit.table.cellShape
import hnau.common.projector.utils.Icon
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.cancel
import hnau.pinfin.projector.resources.save
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource


class TransactionProjectorMainInfoTimeDelegate(
    scope: CoroutineScope,
    private val model: TransactionModel.MainContent.Time,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {

        val state = rememberTimePickerState(
            initialHour = model.initialTime.hour,
            initialMinute = model.initialTime.minute,
        )

        Table(
            orientation = TableOrientation.Vertical,
        ) {
            Cell {
                TimePicker(
                    state = state,
                    modifier = Modifier
                        .clip(cellShape),
                )
            }
            Subtable {
                Cell {
                    HnauButton(
                        modifier = Modifier.weight(1f),
                        shape = cellShape,
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
                }
                Cell {
                    HnauButton(
                        modifier = Modifier.weight(1f),
                        shape = cellShape,
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
                }
            }
        }
    }

}