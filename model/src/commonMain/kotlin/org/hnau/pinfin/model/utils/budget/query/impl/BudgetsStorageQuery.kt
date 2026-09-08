package org.hnau.pinfin.model.utils.budget.query.impl

import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.query.BudgetQuery
import org.hnau.pinfin.model.utils.budget.query.BudgetSummary
import org.hnau.pinfin.model.utils.budget.query.BudgetsQuery
import org.hnau.pinfin.model.utils.budget.storage.BudgetsStorage

class BudgetsStorageQuery(
    private val budgetsStorage: BudgetsStorage,
) : BudgetsQuery {

    override suspend fun budgets(): List<BudgetSummary> = budgetsStorage
        .list
        .value
        .map { (id, repository) ->
            BudgetSummary(
                id = id,
                title = repository.state.value.info.title,
            )
        }

    override suspend fun budget(
        id: BudgetId,
    ): BudgetQuery? = budgetsStorage
        .list
        .value
        .firstOrNull { it.key == id }
        ?.let { (budgetId, repository) ->
            BudgetStateQuery(
                id = budgetId,
                state = repository.state,
            )
        }
}
