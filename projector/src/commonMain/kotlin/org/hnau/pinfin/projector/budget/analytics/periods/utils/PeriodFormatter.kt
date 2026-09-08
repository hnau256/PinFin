package org.hnau.pinfin.projector.budget.analytics.periods.utils

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month
import kotlinx.datetime.number
import kotlinx.datetime.toJavaLocalDate
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.pinfin.model.utils.analytics.AnalyticsConfig
import org.hnau.pinfin.model.utils.analytics.fold
import org.hnau.pinfin.model.utils.analytics.period.AnalyticsPeriod
import org.hnau.pinfin.model.utils.analytics.period.PeriodDuration
import org.hnau.pinfin.model.utils.analytics.period.PeriodUnit
import org.hnau.pinfin.model.utils.analytics.period.fold
import org.hnau.pinfin.projector.Localization
import java.util.Locale
import java.time.format.DateTimeFormatter as JavaDateTimeFormatterFromJava

/**
 * Заголовок периода (docs/analytics-v2-plan.md, "2.4. Навигация по периодам"): для месяца
 * с 1-го - "Сентябрь 2026", для года с 1 января - "2026", иначе - "10 авг - 9 сен 2026".
 */
fun AnalyticsPeriod.formatPeriod(
    period: LocalDateRange,
): String = fold(
    ifWhole = { formatRange(period) },
    ifMonths = { count, startDay ->
        (count == 1 && startDay == 1 && period.start.day == 1).foldBoolean(
            ifTrue = { formatMonthAndYear(period.start) },
            ifFalse = { formatRange(period) },
        )
    },
    ifYears = { count, startMonth, startDay ->
        (count == 1 && startMonth == Month.JANUARY && startDay == 1 && period.start == yearStartOf(period.start)).foldBoolean(
            ifTrue = { period.start.year.toString() },
            ifFalse = { formatRange(period) },
        )
    },
    ifDays = { _, _ -> formatRange(period) },
)

private fun yearStartOf(
    date: LocalDate,
): LocalDate = LocalDate(date.year, Month.JANUARY, 1)

private val monthYearFormatter: JavaDateTimeFormatterFromJava =
    JavaDateTimeFormatterFromJava.ofPattern("LLLL yyyy", Locale("ru"))

private val shortDateFormatter: JavaDateTimeFormatterFromJava =
    JavaDateTimeFormatterFromJava.ofPattern("d MMM", Locale("ru"))

private fun formatMonthAndYear(
    date: LocalDate,
): String = date
    .toJavaLocalDate()
    .format(monthYearFormatter)
    .replaceFirstChar(Char::uppercase)

private fun formatShort(
    date: LocalDate,
): String = date
    .toJavaLocalDate()
    .format(shortDateFormatter)

private fun formatRange(
    period: LocalDateRange,
): String = "${formatShort(period.start)} – ${formatShort(period.endInclusive)} ${period.endInclusive.year}"

/**
 * Резюме конфига одной строкой для карточки над списком (docs/analytics-v2-plan.md,
 * "Фаза 3", п. 4), например "Месяц с 10-го · по категориям · сумма".
 */
fun AnalyticsConfig.summary(
    localization: Localization,
): String = localization.analyticsConfigSummary(
    period.describe(localization),
    groupBy
        ?.fold(
            ifAccount = { localization.groupByAccounts },
            ifCategory = { localization.groupByCategories },
        )
        ?: localization.onlyTotal,
    operation.fold(
        ifSum = { localization.sum },
        ifAverage = { subperiod -> "${localization.average} (${subperiod.describe(localization)})" },
    ),
)

private fun AnalyticsPeriod.describe(
    localization: Localization,
): String = fold(
    ifWhole = { localization.inclusivePeriod },
    ifMonths = { count, startDay ->
        val base = when (count) {
            1 -> localization.month.replaceFirstChar(Char::uppercase)
            3 -> localization.presetQuarter
            else -> "$count ${localization.months}"
        }
        (startDay == 1).foldBoolean(
            ifTrue = { base },
            ifFalse = { "$base с ${startDay}-го" },
        )
    },
    ifYears = { count, startMonth, startDay ->
        val base = (count == 1).foldBoolean(
            ifTrue = { localization.year.replaceFirstChar(Char::uppercase) },
            ifFalse = { "$count ${localization.years}" },
        )
        (startMonth == Month.JANUARY && startDay == 1).foldBoolean(
            ifTrue = { base },
            ifFalse = { "$base с $startDay ${monthGenitive(startMonth)}" },
        )
    },
    ifDays = { count, anchor ->
        if (count == 7) {
            "${localization.presetWeek} с ${weekdayGenitive(anchor.dayOfWeek)}"
        } else if (count % 7 == 0) {
            "${count / 7} × ${localization.presetWeek.lowercase()} с ${weekdayGenitive(anchor.dayOfWeek)}"
        } else {
            "$count ${localization.days}"
        }
    },
)

private fun PeriodDuration.describe(
    localization: Localization,
): String = when (unit) {
    PeriodUnit.Day -> (count == 1).foldBoolean(ifTrue = { localization.day }, ifFalse = { "$count ${localization.days}" })
    PeriodUnit.Month -> (count == 1).foldBoolean(ifTrue = { localization.month }, ifFalse = { "$count ${localization.months}" })
    PeriodUnit.Year -> (count == 1).foldBoolean(ifTrue = { localization.year }, ifFalse = { "$count ${localization.years}" })
}

private val monthGenitives = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря",
)

private fun monthGenitive(
    month: Month,
): String = monthGenitives.getOrElse(month.number - 1) { month.name }

private fun weekdayGenitive(
    dayOfWeek: DayOfWeek,
): String = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "понедельника"
    DayOfWeek.TUESDAY -> "вторника"
    DayOfWeek.WEDNESDAY -> "среды"
    DayOfWeek.THURSDAY -> "четверга"
    DayOfWeek.FRIDAY -> "пятницы"
    DayOfWeek.SATURDAY -> "субботы"
    DayOfWeek.SUNDAY -> "воскресенья"
    else -> dayOfWeek.name
}
