package hnau.pinfin.projector.transaction.pageable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.ItemsRow
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.transaction.pageable.TimeModel
import hnau.pinfin.projector.transaction_old_2.part.PartDefaults
import hnau.pinfin.projector.utils.Label
import hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalTime

class TimeProjector(
    scope: CoroutineScope,
    private val model: TimeModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val dateTimeFormatter: DateTimeFormatter
    }

    class Page(
        scope: CoroutineScope,
        private val model: TimeModel.Page,
        dependencies: Dependencies,
    ) {

        @Pipe
        interface Dependencies

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun Content(
            modifier: Modifier,
            contentPadding: PaddingValues,
        ) {
            Box(
                modifier = modifier
                    .padding(contentPadding),
            ) {
                val initialTime by model.time.collectAsState()
                val state = rememberTimePickerState(
                    initialHour = initialTime.hour,
                    initialMinute = initialTime.minute,
                )
                val hour = state.hour
                val minute = state.minute
                LaunchedEffect(hour, minute) {
                    model.time.value = LocalTime(
                        hour = hour,
                        minute = minute,
                        second = 0,
                        nanosecond = 0,
                    )

                }
                TimePicker(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }


    @Composable
    fun Content(
        modifier: Modifier,
    ) {
        Label(
            modifier = modifier,
            selected = model.isFocused.collectAsState().value,
            onClick = model.requestFocus,
            containerColor = PartDefaults.background,
        ) {
            ItemsRow {
                Icon(Icons.Filled.Schedule)
                Text(
                    text = model
                        .time
                        .collectAsState()
                        .value
                        .let(dependencies.dateTimeFormatter::formatTime),
                )
            }
        }
    }
}