package org.hnau.pinfin.model.utils.analytics.period

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [PeriodDuration.subperiodsOf] выравнивает подпериоды по началу переданного периода
 * (docs/analytics-v2-plan.md, "2.5. Расчёт"), а не по глобальной фазе, и НЕ обрезает
 * последний подпериод по концу периода: вызывающий код ([calcPeriod]) сам решает,
 * какие подпериоды считать "полными", сравнивая их календарные границы с периодом
 * страницы и с диапазоном данных.
 */
class PeriodDurationExtTest {

    @Test
    fun monthlySubperiodsTileExactlyFromPeriodStart() {
        val duration = PeriodDuration(count = 1, unit = PeriodUnit.Month)
        val period = LocalDate(2026, 1, 15)..LocalDate(2026, 4, 14)

        assertEquals(
            listOf(
                LocalDate(2026, 1, 15)..LocalDate(2026, 2, 14),
                LocalDate(2026, 2, 15)..LocalDate(2026, 3, 14),
                LocalDate(2026, 3, 15)..LocalDate(2026, 4, 14),
            ),
            duration.subperiodsOf(period).toList(),
        )
    }

    @Test
    fun lastSubperiodIsNotClippedToPeriodEnd() {
        val duration = PeriodDuration(count = 1, unit = PeriodUnit.Month)
        // Период короче двух месяцев ("с 15 января по 1 марта"), но последний подпериод
        // "15 февраля - 14 марта" целиком не помещается в него - это осознанно (см. KDoc).
        val period = LocalDate(2026, 1, 15)..LocalDate(2026, 3, 1)

        assertEquals(
            listOf(
                LocalDate(2026, 1, 15)..LocalDate(2026, 2, 14),
                LocalDate(2026, 2, 15)..LocalDate(2026, 3, 14),
            ),
            duration.subperiodsOf(period).toList(),
        )
    }

    @Test
    fun weeklySubperiodsTileExactly() {
        val duration = PeriodDuration(count = 7, unit = PeriodUnit.Day)
        val period = LocalDate(2026, 1, 5)..LocalDate(2026, 1, 25)

        assertEquals(
            listOf(
                LocalDate(2026, 1, 5)..LocalDate(2026, 1, 11),
                LocalDate(2026, 1, 12)..LocalDate(2026, 1, 18),
                LocalDate(2026, 1, 19)..LocalDate(2026, 1, 25),
            ),
            duration.subperiodsOf(period).toList(),
        )
    }

    @Test
    fun yearlySubperiodsClampFebruary29() {
        val duration = PeriodDuration(count = 1, unit = PeriodUnit.Year)
        val period = LocalDate(2024, 2, 29)..LocalDate(2026, 2, 27)

        assertEquals(
            listOf(
                LocalDate(2024, 2, 29)..LocalDate(2025, 2, 27),
                LocalDate(2025, 2, 28)..LocalDate(2026, 2, 27),
            ),
            duration.subperiodsOf(period).toList(),
        )
    }
}
