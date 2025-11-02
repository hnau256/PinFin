package hnau.pinfin.model.budget.analytics.tab

import hnau.common.gen.enumvalues.annotations.EnumValues

@EnumValues
enum class AnalyticsTab {
    Accounts,
    Categories,
    Graph,
    ;
    companion object {

        val default: AnalyticsTab
            get() = Accounts
    }
}