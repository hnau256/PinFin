package hnau.pinfin.client.model.budget

import hnau.common.app.goback.GoBackHandlerProvider
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
    }
}