package org.hnau.pinfin.model.budget.analytics.tab

import org.hnau.commons.gen.enumvalues.annotations.EnumValues
import org.hnau.commons.gen.fold.annotations.Fold

@Fold
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