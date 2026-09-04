package org.hnau.pinfin.model.utils.analytics.period

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrThrow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus

/**
 * Период, содержащий [date]. Границы считаются индексной арифметикой в единице периода
 * (см. docs/analytics-v2-plan.md, "2.1. Модель периода") — без накопления ошибки округления дат.
 *
 * [AnalyticsPeriod.Whole] не хранит собственного диапазона, поэтому для него обязателен [wholeRange]
 * (обычно — весь диапазон данных бюджета: первая транзакция .. последняя транзакция).
 */
fun AnalyticsPeriod.periodContaining(
    date: LocalDate,
    wholeRange: LocalDateRange? = null,
): LocalDateRange = fold(
    ifWhole = {
        requireNotNull(wholeRange) {
            "AnalyticsPeriod.Whole.periodContaining requires wholeRange"
        }
    },
    ifMonths = { count, startDay ->
        monthsPeriodContaining(count = count, startDay = startDay, date = date)
    },
    ifYears = { count, startMonth, startDay ->
        yearsPeriodContaining(count = count, startMonth = startMonth, startDay = startDay, date = date)
    },
    ifDays = { count, anchor ->
        daysPeriodContaining(count = count, anchor = anchor, date = date)
    },
)

/** Период, следующий сразу за [period]. Для [AnalyticsPeriod.Whole] — тот же период (следующего нет). */
fun AnalyticsPeriod.next(
    period: LocalDateRange,
): LocalDateRange = fold(
    ifWhole = { period },
    ifMonths = { _, _ -> periodContaining(period.endInclusive + oneDay) },
    ifYears = { _, _, _ -> periodContaining(period.endInclusive + oneDay) },
    ifDays = { _, _ -> periodContaining(period.endInclusive + oneDay) },
)

/** Период, предшествующий [period]. Для [AnalyticsPeriod.Whole] — тот же период (предыдущего нет). */
fun AnalyticsPeriod.previous(
    period: LocalDateRange,
): LocalDateRange = fold(
    ifWhole = { period },
    ifMonths = { _, _ -> periodContaining(period.start - oneDay) },
    ifYears = { _, _, _ -> periodContaining(period.start - oneDay) },
    ifDays = { _, _ -> periodContaining(period.start - oneDay) },
)

/**
 * Периоды от периода, содержащего [range].start, до периода, содержащего [range].endInclusive,
 * без обрезки по данным. Для [AnalyticsPeriod.Whole] — единственный период, равный [range]
 * (обычно [range] и есть весь диапазон данных).
 */
fun AnalyticsPeriod.periods(
    range: LocalDateRange,
): NonEmptyList<LocalDateRange> = fold(
    ifWhole = { nonEmptyListOfPeriod(range) },
    ifMonths = { _, _ -> periodsByAdvancing(range) },
    ifYears = { _, _, _ -> periodsByAdvancing(range) },
    ifDays = { _, _ -> periodsByAdvancing(range) },
)

private fun nonEmptyListOfPeriod(
    range: LocalDateRange,
): NonEmptyList<LocalDateRange> = listOf(range).toNonEmptyListOrThrow()

private fun AnalyticsPeriod.periodsByAdvancing(
    range: LocalDateRange,
): NonEmptyList<LocalDateRange> {
    val periods = buildList {
        var current = periodContaining(range.start)
        add(current)
        while (range.endInclusive !in current) {
            current = next(current)
            add(current)
        }
    }
    return periods.toNonEmptyListOrThrow()
}

internal val oneDay: DatePeriod = DatePeriod(days = 1)

private fun isLeapYear(
    year: Int,
): Boolean = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

internal fun monthLength(
    year: Int,
    monthNumber: Int,
): Int = when (monthNumber) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> 30
}

private fun monthsPeriodContaining(
    count: Int,
    startDay: Int,
    date: LocalDate,
): LocalDateRange {
    val m = date.year * 12 + (date.month.number - 1)
    // Период начинается не с 1-го числа месяца, а с startDay — поэтому сперва находим
    // "месячный слот" (границы которого — startDay каждого календарного месяца),
    // которому принадлежит date, и только потом группируем слоты по count (как в yearsPeriodContaining).
    val candidateStart = monthsStart(absoluteMonth = m, startDay = startDay)
    val effectiveM = if (date < candidateStart) m - 1 else m
    val i = effectiveM.floorDiv(count)
    val start = monthsStart(absoluteMonth = i * count, startDay = startDay)
    val end = monthsStart(absoluteMonth = (i + 1) * count, startDay = startDay) - oneDay
    return start..end
}

internal fun monthsStart(
    absoluteMonth: Int,
    startDay: Int,
): LocalDate {
    val year = absoluteMonth.floorDiv(12)
    val monthNumber = absoluteMonth.mod(12) + 1
    val length = monthLength(year = year, monthNumber = monthNumber)
    return LocalDate(year, monthNumber, minOf(startDay, length))
}

private fun yearsPeriodContaining(
    count: Int,
    startMonth: Month,
    startDay: Int,
    date: LocalDate,
): LocalDateRange {
    val candidateStart = yearStart(year = date.year, startMonth = startMonth, startDay = startDay)
    val effectiveYear = if (date < candidateStart) date.year - 1 else date.year
    val i = effectiveYear.floorDiv(count)
    val start = yearStart(year = i * count, startMonth = startMonth, startDay = startDay)
    val end = yearStart(year = (i + 1) * count, startMonth = startMonth, startDay = startDay) - oneDay
    return start..end
}

internal fun yearStart(
    year: Int,
    startMonth: Month,
    startDay: Int,
): LocalDate {
    val length = monthLength(year = year, monthNumber = startMonth.number)
    return LocalDate(year, startMonth, minOf(startDay, length))
}

private fun daysPeriodContaining(
    count: Int,
    anchor: LocalDate,
    date: LocalDate,
): LocalDateRange {
    val k = anchor.daysUntil(date).floorDiv(count)
    val start = anchor + DatePeriod(days = k * count)
    val end = start + DatePeriod(days = count - 1)
    return start..end
}
