package org.hnau.pinfin.model.sync.client

import org.hnau.pinfin.data.BudgetId

fun interface BudgetSyncOpener {

    fun openBudgetToSync(
        budgetId: BudgetId,
    )
}