package hnau.pinfin.projector.transaction.pageable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import hnau.common.app.projector.uikit.ItemsRow
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.transaction.pageable.DateModel
import hnau.pinfin.projector.transaction.utils.PartDefaults
import hnau.pinfin.projector.utils.Label
import hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class DateProjector(
    scope: CoroutineScope,
    private val model: DateModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val dateTimeFormatter: DateTimeFormatter
    }

    class Page(
        scope: CoroutineScope,
        private val model: DateModel.Page,
    ) {


        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun Content(
            modifier: Modifier = Modifier,
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
                    selected
                        ?.let(Instant.Companion::fromEpochMilliseconds)
                        ?.toLocalDateTime(TimeZone.currentSystemDefault())
                        ?.date
                        ?.let(model.date::value::set)
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


    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        Label(
            modifier = modifier,
            selected = model.isFocused.collectAsState().value,
            onClick = model.requestFocus,
            containerColor = PartDefaults.background,
        ) {
            ItemsRow {
                Icon(Icons.Filled.CalendarMonth)
                Text(
                    text = model
                        .date
                        .collectAsState()
                        .value
                        .let(dependencies.dateTimeFormatter::formatDate),
                    maxLines = 1,
                )
            }
        }
    }
}