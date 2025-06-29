package hnau.pinfin.projector.transaction

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
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
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.days


class TransactionProjectorMainInfoDateDelegate(
    scope: CoroutineScope,
    private val model: TransactionModel.MainContent.Date,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {

        val state = rememberDatePickerState(
            initialSelectedDateMillis = model
                .initialDate
                .atStartOfDayIn(TimeZone.currentSystemDefault())
                .plus(0.5.days)
                .toEpochMilliseconds()
        )

        Table(
            orientation = TableOrientation.Vertical,
        ) {
            Cell {
                DatePicker(
                    state = state,
                    modifier = Modifier
                        .clip(cellShape)
                        .size(480.dp),
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
                                state
                                    .selectedDateMillis
                                    ?.let(Instant.Companion::fromEpochMilliseconds)
                                    ?.toLocalDateTime(TimeZone.currentSystemDefault())
                                    ?.date
                                    ?: model.initialDate
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