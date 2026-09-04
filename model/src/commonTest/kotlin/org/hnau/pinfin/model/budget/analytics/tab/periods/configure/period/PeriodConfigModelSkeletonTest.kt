package org.hnau.pinfin.model.budget.analytics.tab.periods.configure.period

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.hnau.pinfin.model.utils.analytics.period.AnalyticsPeriod
import org.hnau.pinfin.model.utils.analytics.period.PeriodUnit
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Соответствие пресет <-> [AnalyticsPeriod] (docs/analytics-v2-plan.md, "2.2"): открывая
 * экран настроек с уже сохранённым конфигом, [PeriodConfigModel.Skeleton.create] должен
 * узнать "готовый" пресет (чтобы чипы отражали реальное состояние) и посадить "Свой"
 * только на нестандартные count/unit, при этом всегда сохраняя якорь из исходного периода
 * (проблема 1 из плана - якорь пресетов тоже редактируем, не только у "Своего").
 */
class PeriodConfigModelSkeletonTest {

    private val firstTransactionDate = LocalDate(2024, 3, 15)
    private val lastTransactionDate = LocalDate(2026, 9, 4)

    private fun skeletonFor(
        period: AnalyticsPeriod,
    ): PeriodConfigModel.Skeleton = PeriodConfigModel.Skeleton.create(
        initial = period,
        firstTransactionDate = firstTransactionDate,
        lastTransactionDate = lastTransactionDate,
    )

    @Test
    fun wholeMapsToWholePreset() {
        val skeleton = skeletonFor(AnalyticsPeriod.Whole)

        assertEquals(PeriodConfigModel.Preset.Whole, skeleton.preset.value)
    }

    @Test
    fun oneMonthMapsToMonthPresetAndKeepsAnchor() {
        val skeleton = skeletonFor(AnalyticsPeriod.Months(count = 1, startDay = 10))

        assertEquals(PeriodConfigModel.Preset.Month, skeleton.preset.value)
        assertEquals(10, skeleton.anchor.monthsStartDay.value)
    }

    @Test
    fun threeMonthsMapsToQuarterPresetAndKeepsAnchor() {
        val skeleton = skeletonFor(AnalyticsPeriod.Months(count = 3, startDay = 10))

        assertEquals(PeriodConfigModel.Preset.Quarter, skeleton.preset.value)
        assertEquals(10, skeleton.anchor.monthsStartDay.value)
    }

    @Test
    fun twoMonthsIsCustom() {
        val skeleton = skeletonFor(AnalyticsPeriod.Months(count = 2, startDay = 1))

        assertEquals(PeriodConfigModel.Preset.Custom, skeleton.preset.value)
        assertEquals(PeriodUnit.Month, skeleton.duration.unit.value)
        assertEquals(2, skeleton.duration.count.initial)
    }

    @Test
    fun oneYearMapsToYearPresetAndKeepsAnchor() {
        val skeleton = skeletonFor(
            AnalyticsPeriod.Years(count = 1, startMonth = Month.MARCH, startDay = 5),
        )

        assertEquals(PeriodConfigModel.Preset.Year, skeleton.preset.value)
        assertEquals(Month.MARCH, skeleton.anchor.yearsStartMonth.value)
        assertEquals(5, skeleton.anchor.yearsStartDay.value)
    }

    @Test
    fun twoYearsIsCustom() {
        val skeleton = skeletonFor(
            AnalyticsPeriod.Years(count = 2, startMonth = Month.JANUARY, startDay = 1),
        )

        assertEquals(PeriodConfigModel.Preset.Custom, skeleton.preset.value)
        assertEquals(PeriodUnit.Year, skeleton.duration.unit.value)
    }

    @Test
    fun sevenDaysMapsToWeekPresetAndKeepsAnchor() {
        val anchor = LocalDate(2026, 8, 24)
        val skeleton = skeletonFor(AnalyticsPeriod.Days(count = 7, anchor = anchor))

        assertEquals(PeriodConfigModel.Preset.Week, skeleton.preset.value)
        assertEquals(anchor, skeleton.anchor.daysAnchor.value)
    }

    @Test
    fun fourteenDaysIsCustom() {
        val anchor = LocalDate(2026, 8, 24)
        val skeleton = skeletonFor(AnalyticsPeriod.Days(count = 14, anchor = anchor))

        assertEquals(PeriodConfigModel.Preset.Custom, skeleton.preset.value)
        assertEquals(PeriodUnit.Day, skeleton.duration.unit.value)
        assertEquals(14, skeleton.duration.count.initial)
        assertEquals(anchor, skeleton.anchor.daysAnchor.value)
    }
}
