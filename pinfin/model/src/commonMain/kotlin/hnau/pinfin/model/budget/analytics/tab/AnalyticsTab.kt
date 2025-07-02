package hnau.pinfin.model.budget.analytics.tab

import kotlinx.serialization.Serializable

enum class AnalyticsTab {
    Accounts,
    Categories,
    ;
    companion object {

        val default: AnalyticsTab
            get() = Accounts
    }
}

@Serializable
data class AnalyticsTabValues<T>(
    val accounts: T,
    val categories: T,
) {

    operator fun get(
        tab: AnalyticsTab,
    ): T = when (tab) {
        AnalyticsTab.Accounts -> accounts
        AnalyticsTab.Categories -> categories
    }

    inline fun <O> map(
        transform: (T) -> O,
    ): AnalyticsTabValues<O> = AnalyticsTabValues(
        accounts = transform(accounts),
        categories = transform(categories),
    )
}