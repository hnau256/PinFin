package hnau.pinfin.projector.transaction_old_2.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import hnau.pinfin.model.transaction_old_2.page.DatePageModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class DatePageProjector(
    scope: CoroutineScope,
    private val model: DatePageModel,
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
                .padding(contentPadding),
        ) {
            val state = rememberDatePickerState(
                initialSelectedDateMillis = model
                    .date
                    .collectAsState()
                    .value
                    .atStartOfDayIn(TimeZone.currentSystemDefault())
                    .plus(0.5.days)
                    .toEpochMilliseconds(),
            )
            val selected = state.selectedDateMillis
            LaunchedEffect(selected) {
                /*selected
                    ?.let(Instant.Companion::fromEpochMilliseconds)
                    ?.toLocalDateTime(TimeZone.currentSystemDefault())
                    ?.date
                    ?.let(model.date::value::set)*/
            }
            DatePicker(
                state = state,
                modifier = Modifier.fillMaxSize(),
                colors = DatePickerDefaults.colors(
                    containerColor = Color.Transparent,
                )
            )
        }
    }
}