package org.hnau.pinfin.model.budget.analytics.tab.periods.configure.period

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Дефолт якоря при сбросе единицы "дни" на кратность недели / произвольный интервал
 * (docs/analytics-v2-plan.md, "2.2. Как пользователь задаёт начало периода"):
 * "иначе сбрасывается на дефолт единицы (... понедельник, дата последней транзакции
 * для календаря)".
 */
class AnchorModelTest {

    private val firstTransactionDate = LocalDate(2024, 3, 15) // пятница
    private val lastTransactionDate = LocalDate(2026, 9, 4)

    @Test
    fun weekMultipleDefaultsToNearestMondayNotAfterFirstTransaction() {
        val anchor = defaultDaysAnchor(
            count = 7,
            firstTransactionDate = firstTransactionDate,
            lastTransactionDate = lastTransactionDate,
        )

        assertEquals(DayOfWeek.MONDAY, anchor.dayOfWeek)
        // Ближайший понедельник НЕ ПОЗЖЕ 15 марта 2024 (пятница) - это 11 марта 2024.
        assertEquals(LocalDate(2024, 3, 11), anchor)
    }

    @Test
    fun twoWeeksAlsoDefaultsToWeekday() {
        val anchor = defaultDaysAnchor(
            count = 14,
            firstTransactionDate = firstTransactionDate,
            lastTransactionDate = lastTransactionDate,
        )

        assertEquals(LocalDate(2024, 3, 11), anchor)
    }

    @Test
    fun firstTransactionAlreadyOnMondayIsKept() {
        val monday = LocalDate(2024, 3, 11)

        val anchor = defaultDaysAnchor(
            count = 7,
            firstTransactionDate = monday,
            lastTransactionDate = lastTransactionDate,
        )

        assertEquals(monday, anchor)
    }

    @Test
    fun nonWeekMultipleDefaultsToLastTransactionDate() {
        val anchor = defaultDaysAnchor(
            count = 10,
            firstTransactionDate = firstTransactionDate,
            lastTransactionDate = lastTransactionDate,
        )

        assertEquals(lastTransactionDate, anchor)
    }
}
