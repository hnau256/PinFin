package org.hnau.pinfin.projector.budget.analytics.periods.configure.period

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.hnau.commons.app.projector.uikit.row.ChipsFlowRow
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.budget.analytics.tab.periods.configure.period.AnchorModel
import org.hnau.pinfin.model.utils.analytics.period.fold
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.Label
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * Контрол якоря начала периода - четыре режима по таблице докстринга [AnchorModel]
 * (docs/analytics-v2-plan.md, "2.2"): день месяца / месяц+день / день недели / календарь.
 */
class AnchorProjector(
    private val model: AnchorModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        val unit = model.unit.collectAsState().value
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
        ) {
            Text(
                text = dependencies.localization.periodStart,
                style = MaterialTheme.typography.labelLarge,
            )
            unit.fold(
                ifMonth = { MonthDayControl() },
                ifYear = { YearControl() },
                ifDay = {
                    val isWeekdayMode = model.isWeekdayMode.collectAsState().value
                    if (isWeekdayMode) {
                        WeekdayControl()
                    } else {
                        CalendarControl()
                    }
                },
            )
        }
    }

    @Composable
    private fun MonthDayControl() {
        DayOfMonthStepper(model.monthsStartDay)
    }

    @Composable
    private fun YearControl() {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
        ) {
            ChipsFlowRow(
                all = Month.entries,
            ) { month ->
                val selected = model.yearsStartMonth.collectAsState().value == month
                Label(
                    selected = selected,
                    onClick = { model.yearsStartMonth.value = month },
                ) {
                    Text(monthName(month))
                }
            }
            DayOfMonthStepper(model.yearsStartDay)
        }
    }

    @Composable
    private fun DayOfMonthStepper(
        day: MutableStateFlow<Int>,
    ) {
        val value = day.collectAsState().value
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
        ) {
            OutlinedButton(
                onClick = { day.value = (value - 1).coerceIn(1, 31) },
                enabled = value > 1,
            ) {
                Text("-")
            }
            Text(
                text = dependencies.localization.dayOfMonth + ": " + value,
                style = MaterialTheme.typography.bodyLarge,
            )
            OutlinedButton(
                onClick = { day.value = (value + 1).coerceIn(1, 31) },
                enabled = value < 31,
            ) {
                Text("+")
            }
        }
    }

    @Composable
    private fun WeekdayControl() {
        val selected = model.weekday.collectAsState().value
        ChipsFlowRow(
            all = DayOfWeek.entries,
        ) { dayOfWeek ->
            Label(
                selected = dayOfWeek == selected,
                onClick = { model.selectWeekday(dayOfWeek) },
            ) {
                Text(weekdayLabel(dayOfWeek))
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun CalendarControl() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
        ) {
            val state = rememberDatePickerState(
                initialSelectedDateMillis = model
                    .daysAnchor
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
                    ?.let(model.daysAnchor::value::set)
            }
            DatePicker(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                colors = DatePickerDefaults.colors(
                    containerColor = Color.Transparent,
                ),
            )
        }
    }

    private fun weekdayLabel(
        dayOfWeek: DayOfWeek,
    ): String = dependencies.localization.let { l ->
        when (dayOfWeek) {
            DayOfWeek.MONDAY -> l.weekdayMon
            DayOfWeek.TUESDAY -> l.weekdayTue
            DayOfWeek.WEDNESDAY -> l.weekdayWed
            DayOfWeek.THURSDAY -> l.weekdayThu
            DayOfWeek.FRIDAY -> l.weekdayFri
            DayOfWeek.SATURDAY -> l.weekdaySat
            DayOfWeek.SUNDAY -> l.weekdaySun
            else -> dayOfWeek.name
        }
    }

    private fun monthName(
        month: Month,
    ): String = monthNames.getOrElse(month.number - 1) { month.name }

    companion object {

        private val monthNames = listOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь",
        )
    }
}
