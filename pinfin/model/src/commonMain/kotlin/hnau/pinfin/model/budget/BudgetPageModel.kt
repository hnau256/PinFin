package hnau.pinfin.model.budget

import hnau.common.app.model.goback.GoBackHandler
import hnau.pinfin.model.TransactionsModel
import hnau.pinfin.model.budget.analytics.AnalyticsModel
import hnau.pinfin.model.budget.config.BudgetConfigModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface BudgetPageModel {

    val tab: BudgetTab

    data class Transactions(
        val model: TransactionsModel,
    ) : BudgetPageModel {

        val goBackHandler: GoBackHandler
            get() = model.goBackHandler

        override val tab: BudgetTab
            get() = BudgetTab.Transactions
    }

    data class Analytics(
        val model: AnalyticsModel,
    ) : BudgetPageModel {

        val goBackHandler: GoBackHandler
            get() = model.goBackHandler

        override val tab: BudgetTab
            get() = BudgetTab.Analytics
    }

    data class Config(
        val model: BudgetConfigModel,
    ) : BudgetPageModel {

        val goBackHandler: GoBackHandler
            get() = model.goBackHandler

        override val tab: BudgetTab
            get() = BudgetTab.Config
    }

    @Serializable
    sealed interface Skeleton {

        val tab: BudgetTab

        @Serializable
        @SerialName("transactions")
        data class Transactions(
            val skeleton: TransactionsModel.Skeleton = TransactionsModel.Skeleton.create(),
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