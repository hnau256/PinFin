@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.periods.configure.period

import arrow.core.Option
import arrow.core.flatMap
import arrow.core.some
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.valueOrNone
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.utils.analytics.period.AnalyticsPeriod
import org.hnau.pinfin.model.utils.analytics.period.PeriodDuration
import org.hnau.pinfin.model.utils.analytics.period.PeriodUnit
import org.hnau.pinfin.model.utils.analytics.period.fold

/**
 * Пресеты периода + "Свой" (docs/analytics-v2-plan.md, "2.2", "2.6", `PeriodConfigModel`).
 * Аналог старого `ConfigSplitPeriodModel`, но с якорем, редактируемым независимо от пресета
 * (проблема 1 из плана: "начало любого периода с 1-го числа делает поведение непонятным" -
 * теперь день/месяц/дата начала можно поменять и для готовых пресетов, не только под "Свой").
 */
class PeriodConfigModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    firstTransactionDate: LocalDate,
    lastTransactionDate: LocalDate,
) {

    @Fold
    enum class Preset { Week, Month, Quarter, Year, Whole, Custom }

    @Serializable
    data class Skeleton(
        val initial: AnalyticsPeriod,
        val duration: DurationModel.Skeleton,
        val anchor: AnchorModel.Skeleton,
        val preset: MutableStateFlow<Preset> = presetOf(initial).toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun create(
                initial: AnalyticsPeriod,
                firstTransactionDate: LocalDate,
                lastTransactionDate: LocalDate,
            ): Skeleton {
                val (unit, count) = effectiveDurationOf(initial)
                return Skeleton(
                    initial = initial,
                    duration = DurationModel.Skeleton.create(
                        initial = PeriodDuration(count = count, unit = unit),
                    ),
                    anchor = initial.fold(
                        ifWhole = {
                            AnchorModel.Skeleton.create(
                                daysAnchor = lastTransactionDate,
                            )
                        },
                        ifMonths = { _, startDay ->
                            AnchorModel.Skeleton.create(
                                monthsStartDay = startDay,
                                daysAnchor = lastTransactionDate,
                            )
                        },
                        ifYears = { _, startMonth, startDay ->
                            AnchorModel.Skeleton.create(
                                yearsStartMonth = startMonth,
                                yearsStartDay = startDay,
                                daysAnchor = lastTransactionDate,
                            )
                        },
                        ifDays = { _, anchor ->
                            AnchorModel.Skeleton.create(
                                daysAnchor = anchor,
                            )
                        },
                    ),
                )
            }
        }
    }

    val preset: MutableStateFlow<Preset>
        get() = skeleton.preset

    val duration: DurationModel = DurationModel(
        scope = scope,
        skeleton = skeleton.duration,
    )

    /** Действующая единица периода - от пресета либо от "Своего" (см. класс-докстринг). */
    val effectiveUnit: StateFlow<PeriodUnit> = derivedStateFlowOf(scope) {
        preset.state.fold(
            ifWhole = { PeriodUnit.Month },
            ifWeek = { PeriodUnit.Day },
            ifMonth = { PeriodUnit.Month },
            ifQuarter = { PeriodUnit.Month },
            ifYear = { PeriodUnit.Year },
            ifCustom = { duration.unit.state },
        )
    }

    private val effectiveCount: StateFlow<Int> = derivedStateFlowOf(scope) {
        preset.state.fold(
            ifWhole = { 1 },
            ifWeek = { 7 },
            ifMonth = { 1 },
            ifQuarter = { 3 },
            ifYear = { 1 },
            ifCustom = { duration.bestEffortCount.state },
        )
    }

    val anchor: AnchorModel = AnchorModel(
        scope = scope,
        skeleton = skeleton.anchor,
        unit = effectiveUnit,
        count = effectiveCount,
        firstTransactionDate = firstTransactionDate,
        lastTransactionDate = lastTransactionDate,
    )

    private val periodValueOrNone: StateFlow<Option<AnalyticsPeriod>> = derivedStateFlowOf(scope) {
        preset.state.fold(
            ifWhole = { AnalyticsPeriod.Whole.some() },
            ifWeek = {
                AnalyticsPeriod.Days(count = 7, anchor = anchor.daysAnchor.state).some()
            },
            ifMonth = {
                AnalyticsPeriod.Months(count = 1, startDay = anchor.monthsStartDay.state).some()
            },
            ifQuarter = {
                AnalyticsPeriod.Months(count = 3, startDay = anchor.monthsStartDay.state).some()
            },
            ifYear = {
                AnalyticsPeriod.Years(
                    count = 1,
                    startMonth = anchor.yearsStartMonth.state,
                    startDay = anchor.yearsStartDay.state,
                ).some()
            },
            ifCustom = {
                duration.duration.state.valueOrNone.flatMap { periodDuration ->
                    periodDuration.unit.fold(
                        ifDay = {
                            AnalyticsPeriod.Days(
                                count = periodDuration.count,
                                anchor = anchor.daysAnchor.state,
                            )
                        },
                        ifMonth = {
                            AnalyticsPeriod.Months(
                                count = periodDuration.count,
                                startDay = anchor.monthsStartDay.state,
                            )
                        },
                        ifYear = {
                            AnalyticsPeriod.Years(
                                count = periodDuration.count,
                                startMonth = anchor.yearsStartMonth.state,
                                startDay = anchor.yearsStartDay.state,
                            )
                        },
                    ).some()
                }
            },
        )
    }

    val editablePeriod: StateFlow<Editable<AnalyticsPeriod>> = Editable.create(
        scope = scope,
        valueOrNone = periodValueOrNone,
        initialValueOrNone = skeleton.initial.some(),
    )

    companion object {

        private fun presetOf(
            period: AnalyticsPeriod,
        ): Preset = period.fold(
            ifWhole = { Preset.Whole },
            ifMonths = { count, _ ->
                when (count) {
                    1 -> Preset.Month
                    3 -> Preset.Quarter
                    else -> Preset.Custom
                }
            },
            ifYears = { count, _, _ -> if (count == 1) Preset.Year else Preset.Custom },
            ifDays = { count, _ -> if (count == 7) Preset.Week else Preset.Custom },
        )

        private fun effectiveDurationOf(
            period: AnalyticsPeriod,
        ): Pair<PeriodUnit, Int> = period.fold(
            ifWhole = { PeriodUnit.Month to 1 },
            ifMonths = { count, _ -> PeriodUnit.Month to count },
            ifYears = { count, _, _ -> PeriodUnit.Year to count },
            ifDays = { count, _ -> PeriodUnit.Day to count },
        )
    }
}
