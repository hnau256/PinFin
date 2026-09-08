package org.hnau.pinfin.model.utils.budget.query

import org.hnau.pinfin.data.BudgetId

/** Entry point: all budgets of the app. Knows nothing about MCP or any other consumer. */
interface BudgetsQuery {

    suspend fun budgets(): List<BudgetSummary>

    /** `null` - there is no budget with such id. */
    suspend fun budget(id: BudgetId): BudgetQuery?
}
