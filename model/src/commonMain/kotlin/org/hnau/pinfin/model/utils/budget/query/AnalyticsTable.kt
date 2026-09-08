package org.hnau.pinfin.model.utils.budget.query

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.pinfin.data.Amount

@Serializable
data class DateRangeDto(
    val start: LocalDate,
    val end: LocalDate,
)

@Serializable
data class AnalyticsRow(
    val start: LocalDate,
    val end: LocalDate,
    val partial: Boolean,
    val groups: Map<String, Totals>? = null,
    @SerialName("sum_debit")
    val sumDebit: Amount,
    @SerialName("sum_credit")
    val sumCredit: Amount,
    val sum: SignedAmount,
) {

    companion object {

        fun create(
            range: LocalDateRange,
            partial: Boolean,
            groups: Map<String, Totals>?,
            totals: Totals,
        ): AnalyticsRow = AnalyticsRow(
            start = range.start,
            end = range.endInclusive,
            partial = partial,
            groups = groups,
            sumDebit = totals.sumDebit,
            sumCredit = totals.sumCredit,
            sum = totals.sum,
        )
    }
}

@Serializable
data class AnalyticsTable(
    val range: DateRangeDto? = null,
    @SerialName("group_by")
    val groupBy: GroupBy? = null,
    val aggregation: Aggregation,
    val periods: List<AnalyticsRow>,
    @SerialName("sum_debit")
    val sumDebit: Amount,
    @SerialName("sum_credit")
    val sumCredit: Amount,
    val sum: SignedAmount,
) {

    companion object {

        fun empty(
            aggregation: Aggregation,
            groupBy: GroupBy?,
        ): AnalyticsTable = AnalyticsTable(
            range = null,
            groupBy = groupBy,
            aggregation = aggregation,
            periods = emptyList(),
            sumDebit = Amount.zero,
            sumCredit = Amount.zero,
            sum = SignedAmount.zero,
        )

        fun create(
            range: LocalDateRange,
            groupBy: GroupBy?,
            aggregation: Aggregation,
            periods: List<AnalyticsRow>,
            totals: Totals,
        ): AnalyticsTable = AnalyticsTable(
            range = DateRangeDto(start = range.start, end = range.endInclusive),
            groupBy = groupBy,
            aggregation = aggregation,
            periods = periods,
            sumDebit = totals.sumDebit,
            sumCredit = totals.sumCredit,
            sum = totals.sum,
        )
    }
}
