package hnau.pinfin.model.budget.analytics.tab

import org.hnau.commons.gen.enumvalues.annotations.EnumValues

@EnumValues
enum class AnalyticsTab {
    Accounts,
    Graph,
    ;
    companion object {

        val default: AnalyticsTab
            get() = Accounts
    }
}