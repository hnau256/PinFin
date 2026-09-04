package org.hnau.pinfin.model.utils.analytics.period

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrThrow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus

/**
 * Разбивает [period] на подпериоды длины `this`, выровненные по [period].start
 * (см. docs/analytics-v2-plan.md, "2.5. Расчёт"). Подпериоды считаются той же
 * индексной арифметикой, что и [AnalyticsPeriod] — без дрейфа дат — но, в отличие
 * от него, привязаны не к глобальной фазе, а к конкретной дате начала [period].
 *
 * Последний подпериод не обрезается по [period].endInclusive: вызывающая сторона
 * (см. [docs/analytics-v2-plan.md] "среднее и неполные подпериоды") сама решает,
 * какие подпериоды считать «полными», сверяя их календарные границы с диапазоном
 * данных и с [period].
 */
fun PeriodDuration.subperiodsOf(
    period: LocalDateRange,
): NonEmptyList<LocalDateRange> {
    val starts = buildList {
        var index = 0
        add(subperiodStart(periodStart = period.start, index = index))
        while (true) {
            index += 1
            val start = subperiodStart(periodStart = period.start, index = index)
            if (start > period.endInclusive) break
            add(start)
        }
    }
    return starts
        .mapIndexed { index, start ->
            val end = subperiodStart(periodStart = period.start, index = index + 1) - oneDay
            start..end
        }
        .toNonEmptyListOrThrow()
}

private fun PeriodDuration.subperiodStart(
    periodStart: LocalDate,
    index: Int,
): LocalDate = when (unit) {
    PeriodUnit.Day -> periodStart + DatePeriod(days = index * count)
    PeriodUnit.Month -> {
        val baseMonth = periodStart.year * 12 + (periodStart.month.number - 1)
        monthsStart(
            absoluteMonth = baseMonth + index * count,
            startDay = periodStart.day,
        )
    }
    PeriodUnit.Year -> yearStart(
        year = periodStart.year + index * count,
        startMonth = periodStart.month,
        startDay = periodStart.day,
    )
}
