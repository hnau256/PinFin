package hnau.pinfin.model.budget.analytics.tab

import hnau.common.gen.enumvalues.annotations.EnumValues

enum class AnalyticsTab {
    Accounts,
    Graph,
    ;
    companion object {

        val default: AnalyticsTab
            get() = Accounts
    }
}