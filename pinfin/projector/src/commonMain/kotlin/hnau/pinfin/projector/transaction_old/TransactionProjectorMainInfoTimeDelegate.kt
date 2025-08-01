package hnau.pinfin.projector.transaction_old

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.table.Subtable
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableDefaults
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.transaction_old.TransactionModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.cancel
import hnau.pinfin.projector.resources.save
import hnau.pipe.annotations.Pipe
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

    @Composable
    fun Content() {

        val state = rememberTimePickerState(
            initialHour = model.initialTime.hour,
            initialMinute = model.initialTime.minute,
        )

        Table(
            orientation = TableOrientation.Vertical,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Cell(
                isLast = false,
            ) { modifier ->
                TimePicker(
                    state = state,
                    modifier = modifier.background(
                        shape = shape,
                        color = TableDefaults.cellColor,
                    ),
                )
            }
            Subtable(
                isLast = true,
            ) {
                Cell(
                    isLast = false,
                ) { modifier ->
                    Button(
                        modifier = modifier.weight(1f),
                        shape = shape,
                        onClick = model.cancel,
                        content = {
                            Icon(Icons.Filled.Clear)
                            Text(
                                text = stringResource(Res.string.cancel)
                            )
                        }
                    )
                }
                Cell(
                    isLast = true,
                ) { modifier ->
                    Button(
                        modifier = modifier.weight(1f),
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
                            Icon(Icons.Filled.Done)
                            Text(
                                text = stringResource(Res.string.save)
                            )
                        }
                    )
                }
            }
        }
    }
}