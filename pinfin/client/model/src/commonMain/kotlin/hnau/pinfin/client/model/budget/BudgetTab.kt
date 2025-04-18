package hnau.pinfin.client.model.budget

import kotlinx.serialization.Serializable

@Serializable
enum class BudgetTab {
    Transactions,
    /*Statistic,
    Config,*/
    ;

    companion object {

        val default: BudgetTab = Transactions
    }
}

@Serializable
data class BudgetTabValues<T>(
    val transactions: T,
    //val statistic: T,
    //val config: T,
) {

    operator fun get(
        tab: BudgetTab,
    ): T = when (tab) {
        BudgetTab.Transactions -> transactions
        /*BudgetTab.Statistic -> statistic
        BudgetTab.Config -> config*/
    }
}
