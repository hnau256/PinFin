package hnau.pinfin.model.utils.analytics

import hnau.pinfin.model.utils.DeferredList
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

fun interface AnalyticsPeriodsProvider {

    fun split(
        inclusive: LocalDateRange,
    ): List<LocalDateRange>

    companion object {

        val inclusive: AnalyticsPeriodsProvider =
            AnalyticsPeriodsProvider(::listOf)

        inline fun lazy(
            crossinline create: (LocalDateRange) -> Pair<Int, (Int) -> LocalDateRange>,
        ): AnalyticsPeriodsProvider = AnalyticsPeriodsProvider { inclusive ->
            val (size, generator) = create(inclusive)
            DeferredList(size, generator)
        }

        fun fixed(
            startOfOneOfPeriods: LocalDate,
            period: DatePeriod,
        ): AnalyticsPeriodsProvider = lazy { inclusive ->

            var firstPeriodStart = startOfOneOfPeriods
            while (firstPeriodStart + period <= inclusive.start) {
                firstPeriodStart = firstPeriodStart + period
            }
            while (firstPeriodStart > inclusive.start) {
                firstPeriodStart = firstPeriodStart - period
            }

            var size = 0
            var cur = firstPeriodStart
            while (cur <= inclusive.endInclusive) {
                size++
                cur = cur + period
            }

            size to { index ->
                var s = firstPeriodStart
                repeat(index) { s = s + period }
                val rawEnd = (s + period) - DatePeriod(days = 1)
                val effectiveStart = maxOf(s, inclusive.start)
                val effectiveEnd = minOf(rawEnd, inclusive.endInclusive)
                effectiveStart..effectiveEnd
            }
        }
    }
}