package org.hnau.pinfin.model.utils.analytics.period

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Проверяет индексную арифметику границ [AnalyticsPeriod] (docs/analytics-v2-plan.md, "2.1"):
 * без дрейфа дат при длительности в месяцах/годах (в отличие от старого [splitToPeriods],
 * задокументированного как баговый в [org.hnau.pinfin.model.utils.analytics.SplitToPeriodsExtTest]),
 * с клампом короткого месяца/февраля и с корректной фазой для count > 1.
 */
class AnalyticsPeriodExtTest {

    @Test
    fun monthsWithStartDayBoundary() {
        val period = AnalyticsPeriod.Months(count = 1, startDay = 10)

        // День до границы (4 сентября) ещё принадлежит периоду "10 авг - 9 сен".
        assertEquals(
            LocalDate(2026, 8, 10)..LocalDate(2026, 9, 9),
            period.periodContaining(LocalDate(2026, 9, 4)),
        )

        // Ровно 10-е число - начало нового периода.
        assertEquals(
            LocalDate(2026, 9, 10)..LocalDate(2026, 10, 9),
            period.periodContaining(LocalDate(2026, 9, 10)),
        )
    }

    @Test
    fun monthsWithStartDay31DoesNotDrift() {
        val period = AnalyticsPeriod.Months(count = 1, startDay = 31)

        // Февраль короткий - день клампится к 28-му (2023 - не високосный).
        assertEquals(
            LocalDate(2023, 1, 31)..LocalDate(2023, 2, 27),
            period.periodContaining(LocalDate(2023, 2, 15)),
        )
        // 28 февраля - всё ещё внутри того же периода "с 31-го", клампированного в феврале.
        assertEquals(
            LocalDate(2023, 1, 31)..LocalDate(2023, 2, 27),
            period.periodContaining(LocalDate(2023, 1, 31)),
        )

        // Следующий период должен снова начаться с 28-го (последний день февраля),
        // а следующий за ним - вернуться к 31-му марта: без накопления ошибки.
        val startsOfConsecutivePeriods = generateSequence(
            period.periodContaining(LocalDate(2023, 1, 31)),
        ) { period.next(it) }
            .take(5)
            .map { it.start }
            .toList()

        assertEquals(
            listOf(
                LocalDate(2023, 1, 31),
                LocalDate(2023, 2, 28),
                LocalDate(2023, 3, 31),
                LocalDate(2023, 4, 30),
                LocalDate(2023, 5, 31),
            ),
            startsOfConsecutivePeriods,
        )
    }

    @Test
    fun quarterPhaseAlignsToJanuary() {
        val quarter = AnalyticsPeriod.Months(count = 3, startDay = 1)

        assertEquals(
            LocalDate(2026, 1, 1)..LocalDate(2026, 3, 31),
            quarter.periodContaining(LocalDate(2026, 2, 1)),
        )
        assertEquals(
            LocalDate(2026, 4, 1)..LocalDate(2026, 6, 30),
            quarter.periodContaining(LocalDate(2026, 4, 1)),
        )
        assertEquals(
            LocalDate(2026, 4, 1)..LocalDate(2026, 6, 30),
            quarter.periodContaining(LocalDate(2026, 6, 30)),
        )
        assertEquals(
            LocalDate(2026, 7, 1)..LocalDate(2026, 9, 30),
            quarter.periodContaining(LocalDate(2026, 7, 1)),
        )
        assertEquals(
            LocalDate(2026, 10, 1)..LocalDate(2026, 12, 31),
            quarter.periodContaining(LocalDate(2026, 12, 31)),
        )
    }

    @Test
    fun yearStartingMarch1CrossesLeapFebruary29() {
        val year = AnalyticsPeriod.Years(count = 1, startMonth = Month.MARCH, startDay = 1)

        // 29 февраля 2024 (високосный год) - последний день периода, начавшегося 1 марта 2023.
        assertEquals(
            LocalDate(2023, 3, 1)..LocalDate(2024, 2, 29),
            year.periodContaining(LocalDate(2024, 2, 29)),
        )
        // 1 марта 2024 - начало следующего периода, который закончится 28 февраля 2025 (не високосный).
        assertEquals(
            LocalDate(2024, 3, 1)..LocalDate(2025, 2, 28),
            year.periodContaining(LocalDate(2024, 3, 1)),
        )
    }

    @Test
    fun yearStartingFebruary29ClampsInNonLeapYears() {
        val year = AnalyticsPeriod.Years(count = 1, startMonth = Month.FEBRUARY, startDay = 29)

        // 2024 - високосный, якорь ровно 29 февраля.
        assertEquals(
            LocalDate(2024, 2, 29)..LocalDate(2025, 2, 27),
            year.periodContaining(LocalDate(2024, 2, 29)),
        )
        // 2025 - не високосный, начало периода клампится к 28 февраля.
        assertEquals(
            LocalDate(2025, 2, 28)..LocalDate(2026, 2, 27),
            year.periodContaining(LocalDate(2025, 2, 28)),
        )
        // День до клампированной границы всё ещё в предыдущем периоде.
        assertEquals(
            LocalDate(2024, 2, 29)..LocalDate(2025, 2, 27),
            year.periodContaining(LocalDate(2025, 2, 27)),
        )
    }

    @Test
    fun days14Boundaries() {
        val twoWeeks = AnalyticsPeriod.Days(count = 14, anchor = LocalDate(2026, 1, 5))

        assertEquals(
            LocalDate(2026, 1, 5)..LocalDate(2026, 1, 18),
            twoWeeks.periodContaining(LocalDate(2026, 1, 5)),
        )
        assertEquals(
            LocalDate(2026, 1, 5)..LocalDate(2026, 1, 18),
            twoWeeks.periodContaining(LocalDate(2026, 1, 18)),
        )
        assertEquals(
            LocalDate(2026, 1, 19)..LocalDate(2026, 2, 1),
            twoWeeks.periodContaining(LocalDate(2026, 1, 19)),
        )
        // Дата раньше якоря - индекс отрицательный, но период всё равно 14 дней подряд.
        assertEquals(
            LocalDate(2025, 12, 22)..LocalDate(2026, 1, 4),
            twoWeeks.periodContaining(LocalDate(2026, 1, 4)),
        )
    }

    @Test
    fun wholePeriodRequiresExplicitRange() {
        val whole = AnalyticsPeriod.Whole
        val range = LocalDate(2020, 1, 1)..LocalDate(2026, 9, 4)

        assertEquals(
            range,
            whole.periodContaining(date = LocalDate(2023, 5, 5), wholeRange = range),
        )
        assertFailsWith<IllegalArgumentException> {
            whole.periodContaining(date = LocalDate(2023, 5, 5))
        }

        assertEquals(
            listOf(range),
            whole.periods(range).toList(),
        )

        // Для "Whole" следующего/предыдущего периода не существует - возвращается тот же период.
        assertEquals(range, whole.next(range))
        assertEquals(range, whole.previous(range))
    }

    @Test
    fun nextAndPreviousAreContiguousAndInvertible() {
        val period = AnalyticsPeriod.Months(count = 1, startDay = 10)
        val current = period.periodContaining(LocalDate(2026, 9, 4))

        val next = period.next(current)
        val previous = period.previous(current)

        assertTrue(LocalDate(2026, 9, 4) in current)
        assertEquals(current.endInclusive.plusOneDay(), next.start)
        assertEquals(current.start.minusOneDay(), previous.endInclusive)
        assertEquals(current, period.previous(next))
        assertEquals(current, period.next(previous))
    }

    @Test
    fun periodsSpansFromRangeStartToRangeEndInclusive() {
        val period = AnalyticsPeriod.Months(count = 1, startDay = 1)
        val range = LocalDate(2026, 1, 1)..LocalDate(2026, 4, 15)

        val periods = period.periods(range)

        assertEquals(
            listOf(
                LocalDate(2026, 1, 1)..LocalDate(2026, 1, 31),
                LocalDate(2026, 2, 1)..LocalDate(2026, 2, 28),
                LocalDate(2026, 3, 1)..LocalDate(2026, 3, 31),
                LocalDate(2026, 4, 1)..LocalDate(2026, 4, 30),
            ),
            periods.toList(),
        )
    }

    private fun LocalDate.plusOneDay(): LocalDate = this + DatePeriod(days = 1)

    private fun LocalDate.minusOneDay(): LocalDate = this - DatePeriod(days = 1)
}
