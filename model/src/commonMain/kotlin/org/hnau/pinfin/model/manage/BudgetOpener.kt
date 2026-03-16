package org.hnau.pinfin.model.manage

import org.hnau.pinfin.data.BudgetId

fun interface BudgetOpener {

    suspend fun openBudget(
        budgetId: BudgetId,
    )
}