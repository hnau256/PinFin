package org.hnau.pinfin.model.utils.budget.query

import org.hnau.pinfin.data.BudgetId

/** Data of a single budget. Implementation itself hops to a background dispatcher. */
interface BudgetQuery {

    val id: BudgetId

    suspend fun metadata(): BudgetMetadata

    suspend fun records(
        filter: RecordsFilter?,
        offset: Int,
        limit: Int,
    ): RecordsPage

    suspend fun analytics(
        filter: RecordsFilter?,
        period: PeriodSpec,
        groupBy: GroupBy?,
        aggregation: Aggregation,
    ): AnalyticsTable
}
