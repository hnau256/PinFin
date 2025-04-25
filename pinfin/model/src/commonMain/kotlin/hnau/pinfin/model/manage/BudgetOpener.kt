package hnau.pinfin.model.manage

import hnau.pinfin.upchain.BudgetId

fun interface BudgetOpener {

    suspend fun openBudget(
        budgetId: BudgetId,
    )
}