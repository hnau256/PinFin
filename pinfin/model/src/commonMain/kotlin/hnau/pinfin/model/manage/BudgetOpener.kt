package hnau.pinfin.model.manage

import hnau.pinfin.data.BudgetId

fun interface BudgetOpener {

    suspend fun openBudget(
        budgetId: BudgetId,
    )
}