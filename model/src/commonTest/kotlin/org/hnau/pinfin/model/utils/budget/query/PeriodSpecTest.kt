package org.hnau.pinfin.model.utils.budget.query

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.hnau.pinfin.model.utils.analytics.period.AnalyticsPeriod
import org.hnau.pinfin.model.utils.analytics.period.PeriodDuration
import org.hnau.pinfin.model.utils.analytics.period.PeriodUnit
import org.hnau.pinfin.model.utils.budget.query.calc.toAnalyticsPeriod
import org.hnau.pinfin.model.utils.budget.query.calc.toPeriodDuration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PeriodSpecTest {

    @Test
    fun mapsWholeToAnalyticsPeriodWhole() {
        assertEquals(AnalyticsPeriod.Whole, PeriodSpec.Whole.toAnalyticsPeriod())
    }

    @Test
    fun mapsMonthsToAnalyticsPeriodMonths() {
        assertEquals(
            AnalyticsPeriod.Months(count = 3, startDay = 1),
            PeriodSpec.Months(count = 3, startDay = 1).toAnalyticsPeriod(),
        )
    }

    @Test
    fun mapsYearsToAnalyticsPeriodYears() {
        assertEquals(
            AnalyticsPeriod.Years(count = 1, startMonth = Month.MARCH, startDay = 1),
            PeriodSpec.Years(count = 1, startMonth = 3, startDay = 1).toAnalyticsPeriod(),
        )
    }

    @Test
    fun mapsDaysToAnalyticsPeriodDays() {
        val anchor = LocalDate(2026, 1, 5)
        assertEquals(
            AnalyticsPeriod.Days(count = 7, anchor = anchor),
            PeriodSpec.Days(count = 7, anchor = anchor).toAnalyticsPeriod(),
        )
    }

    @Test
    fun mapsSubperiodSpecToPeriodDuration() {
        assertEquals(
            PeriodDuration(count = 2, unit = PeriodUnit.Year),
            SubperiodSpec(count = 2, unit = SubperiodUnit.Year).toPeriodDuration(),
        )
    }

    @Test
    fun rejectsInvalidCount() {
        assertFailsWith<IllegalArgumentException> {
            PeriodSpec.Months(count = 0, startDay = 1).toAnalyticsPeriod()
        }
    }

    @Test
    fun rejectsInvalidStartDay() {
        assertFailsWith<IllegalArgumentException> {
            PeriodSpec.Months(count = 1, startDay = 40).toAnalyticsPeriod()
        }
    }

    @Test
    fun rejectsInvalidStartMonth() {
        assertFailsWith<IllegalArgumentException> {
            PeriodSpec.Years(count = 1, startMonth = 13, startDay = 1).toAnalyticsPeriod()
        }
    }
}
