package hnau.pinfin.projector.transaction.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import hnau.pinfin.model.transaction.part.page.TimePageModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalTime

class TimePageProjector(
    scope: CoroutineScope,
    private val model: TimePageModel,
    dependencies: Dependencies,
) : PageProjector {

    @Pipe
    interface Dependencies

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
        modifier: Modifier,
        contentPadding: PaddingValues,
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
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
                model.onTimeChanged(
                    LocalTime(
                        hour = hour,
                        minute = minute,
                        second = 0,
                        nanosecond = 0,
                    )
                )
            }
            TimePicker(
                state = state,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}