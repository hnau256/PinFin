package hnau.pinfin.model.budget

import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.pinfin.model.TransactionsModel
import hnau.pinfin.model.budget.analytics.AnalyticsModel
import hnau.pinfin.model.budget.config.BudgetConfigModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface BudgetPageModel : GoBackHandlerProvider {

    val tab: BudgetTab

    data class Transactions(
        val model: TransactionsModel,
    ) : BudgetPageModel, GoBackHandlerProvider by model {

        override val tab: BudgetTab
            get() = BudgetTab.Transactions
    }

    data class Analytics(
        val model: AnalyticsModel,
    ) : BudgetPageModel, GoBackHandlerProvider by model {

        override val tab: BudgetTab
            get() = BudgetTab.Analytics
    }

    data class Config(
        val model: BudgetConfigModel,
    ) : BudgetPageModel, GoBackHandlerProvider by model {

        override val tab: BudgetTab
            get() = BudgetTab.Config
    }

    @Serializable
    sealed interface Skeleton {

        val tab: BudgetTab

        @Serializable
        @SerialName("transactions")
        data class Transactions(
            val skeleton: TransactionsModel.Skeleton = TransactionsModel.Skeleton(),
        ) : Skeleton {

            override val tab: BudgetTab
                get() = BudgetTab.Transactions
        }

        @Serializable
        @SerialName("analytics")
        data class Analytics(
            val skeleton: AnalyticsModel.Skeleton = AnalyticsModel.Skeleton(),
        ) : Skeleton {

            override val tab: BudgetTab
                get() = BudgetTab.Analytics
        }

        @Serializable
        @SerialName("config")
        data class Config(
            val skeleton: BudgetConfigModel.Skeleton = BudgetConfigModel.Skeleton(),
        ) : Skeleton {

            override val tab: BudgetTab
                get() = BudgetTab.Config
        }
    }
}