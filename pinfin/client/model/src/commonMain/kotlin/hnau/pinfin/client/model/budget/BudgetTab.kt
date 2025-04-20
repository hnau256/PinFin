package hnau.pinfin.client.model.budget

import kotlinx.serialization.Serializable

@Serializable
enum class BudgetTab {
    Transactions,
    Analytics,
    Config,
    ;

    companion object {

        val default: BudgetTab = Transactions
    }
}

@Serializable
data class BudgetTabValues<T>(
    val transactions: T,
    val analytics: T,
    val config: T,
) {

    operator fun get(
        tab: BudgetTab,
    ): T = when (tab) {
        BudgetTab.Transactions -> transactions
        BudgetTab.Analytics -> analytics
        BudgetTab.Config -> config
    }
}
