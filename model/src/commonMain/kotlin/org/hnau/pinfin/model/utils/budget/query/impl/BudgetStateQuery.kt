package org.hnau.pinfin.model.utils.budget.query.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.query.Aggregation
import org.hnau.pinfin.model.utils.budget.query.AnalyticsTable
import org.hnau.pinfin.model.utils.budget.query.BudgetMetadata
import org.hnau.pinfin.model.utils.budget.query.BudgetQuery
import org.hnau.pinfin.model.utils.budget.query.GroupBy
import org.hnau.pinfin.model.utils.budget.query.PeriodSpec
import org.hnau.pinfin.model.utils.budget.query.RecordsFilter
import org.hnau.pinfin.model.utils.budget.query.RecordsPage
import org.hnau.pinfin.model.utils.budget.query.calc.calcAnalyticsTable
import org.hnau.pinfin.model.utils.budget.query.calc.metadata
import org.hnau.pinfin.model.utils.budget.query.calc.queryRecords
import org.hnau.pinfin.model.utils.budget.state.BudgetState

class BudgetStateQuery(
    override val id: BudgetId,
    private val state: StateFlow<BudgetState>,
) : BudgetQuery {

    override suspend fun metadata(): BudgetMetadata = calc { it.metadata(id) }

    override suspend fun records(
        filter: RecordsFilter?,
        offset: Int,
        limit: Int,
    ): RecordsPage = calc { queryRecords(it, filter, offset, limit) }

    override suspend fun analytics(
        filter: RecordsFilter?,
        period: PeriodSpec,
        groupBy: GroupBy?,
        aggregation: Aggregation,
    ): AnalyticsTable = calc { calcAnalyticsTable(it, filter, period, groupBy, aggregation) }

    private suspend inline fun <T> calc(
        crossinline block: (BudgetState) -> T,
    ): T = withContext(Dispatchers.Default) { block(state.value) }
}
