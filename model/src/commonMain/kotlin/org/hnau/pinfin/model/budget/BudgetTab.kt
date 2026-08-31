package org.hnau.pinfin.model.budget

import kotlinx.serialization.Serializable
import org.hnau.commons.gen.fold.annotations.Fold

@Fold
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
