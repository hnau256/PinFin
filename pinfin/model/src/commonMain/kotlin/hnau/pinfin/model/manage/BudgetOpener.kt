package hnau.pinfin.model.manage

import hnau.pinfin.data.dto.BudgetId

fun interface BudgetOpener {

    suspend fun openBudget(
        budgetId: BudgetId,
    )
}