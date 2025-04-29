package hnau.pinfin.model.sync.client

import hnau.pinfin.data.BudgetId

fun interface BudgetSyncOpener {

    fun openBudgetToSync(
        budgetId: BudgetId,
    )
}