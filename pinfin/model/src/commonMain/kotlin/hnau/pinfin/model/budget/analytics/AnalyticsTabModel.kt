package hnau.pinfin.model.budget.analytics

import hnau.common.model.goback.GoBackHandlerProvider
import hnau.pinfin.model.budget.analytics.tab.AccountsModel
import hnau.pinfin.model.budget.analytics.tab.AnalyticsTab
import hnau.pinfin.model.budget.analytics.tab.CategoriesModel

sealed interface AnalyticsTabModel : GoBackHandlerProvider {

    val key: AnalyticsTab

    data class Accounts(
        val model: AccountsModel,
    ) : AnalyticsTabModel, GoBackHandlerProvider by model {

        override val key: AnalyticsTab
            get() = AnalyticsTab.Accounts
    }

    data class Categories(
        val model: CategoriesModel,
    ) : AnalyticsTabModel, GoBackHandlerProvider by model {

        override val key: AnalyticsTab
            get() = AnalyticsTab.Categories
    }
}