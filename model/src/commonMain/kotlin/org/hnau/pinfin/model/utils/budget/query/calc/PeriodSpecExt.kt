package org.hnau.pinfin.model.utils.budget.query.calc

import kotlinx.datetime.Month
import org.hnau.pinfin.model.utils.analytics.period.AnalyticsPeriod
import org.hnau.pinfin.model.utils.analytics.period.PeriodDuration
import org.hnau.pinfin.model.utils.analytics.period.PeriodUnit
import org.hnau.pinfin.model.utils.budget.query.PeriodSpec
import org.hnau.pinfin.model.utils.budget.query.SubperiodSpec
import org.hnau.pinfin.model.utils.budget.query.SubperiodUnit
import org.hnau.pinfin.model.utils.budget.query.fold

internal fun PeriodSpec.toAnalyticsPeriod(): AnalyticsPeriod = fold(
    ifWhole = { AnalyticsPeriod.Whole },
    ifMonths = { count, startDay ->
        require(count >= 1) { "period.count must be >= 1" }
        require(startDay in 1..31) { "period.start_day must be in 1..31" }
        AnalyticsPeriod.Months(count = count, startDay = startDay)
    },
    ifYears = { count, startMonth, startDay ->
        require(count >= 1) { "period.count must be >= 1" }
        require(startMonth in 1..12) { "period.start_month must be in 1..12" }
        require(startDay in 1..31) { "period.start_day must be in 1..31" }
        AnalyticsPeriod.Years(count = count, startMonth = Month(startMonth), startDay = startDay)
    },
    ifDays = { count, anchor ->
        require(count >= 1) { "period.count must be >= 1" }
        AnalyticsPeriod.Days(count = count, anchor = anchor)
    },
)

internal fun SubperiodSpec.toPeriodDuration(): PeriodDuration {
    require(count >= 1) { "aggregation.subperiod.count must be >= 1" }
    return PeriodDuration(
        count = count,
        unit = unit.fold(
            ifDay = { PeriodUnit.Day },
            ifMonth = { PeriodUnit.Month },
            ifYear = { PeriodUnit.Year },
        ),
    )
}
