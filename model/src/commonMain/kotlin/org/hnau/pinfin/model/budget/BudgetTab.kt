package org.hnau.pinfin.model.budget

import kotlinx.serialization.Serializable

@Serializable
enum class BudgetTab {
    Transactions,
    Analytics,
    Manage,
    ;

    companion object {

        val default: BudgetTab = Transactions
    }
}
