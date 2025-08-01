package hnau.pinfin.projector.transaction_old

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import hnau.common.app.projector.uikit.table.Subtable
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.transaction_old.TransactionModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.cancel
import hnau.pinfin.projector.resources.save
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant


@OptIn(ExperimentalMaterial3Api::class)
class TransactionProjectorMainInfoDateDelegate(
    scope: CoroutineScope,
    private val model: TransactionModel.MainContent.Date,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

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
            modifier = Modifier.fillMaxWidth(),
        ) {
            Cell(
                isLast = false,
            ) {
                DatePicker(
                    state = state,
                    modifier = Modifier
                        .clip(shape)
                        .size(480.dp),
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
                                state
                                    .selectedDateMillis
                                    ?.let(Instant.Companion::fromEpochMilliseconds)
                                    ?.toLocalDateTime(TimeZone.currentSystemDefault())
                                    ?.date
                                    ?: model.initialDate
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