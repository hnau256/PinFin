package hnau.pinfin.model.budgetsstack

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.model.budgetslist.BudgetsListModel
import hnau.pinfin.model.LoadBudgetModel
import hnau.pinfin.model.budget.TransactionsModel
import hnau.pinfin.model.transaction.TransactionModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface BudgetsStackElementModel : GoBackHandlerProvider {

    val key: Int

    data class Budgets(
        val model: BudgetsListModel,
    ) : BudgetsStackElementModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 0
    }

    data class Budget(
        val model: LoadBudgetModel,
    ) : BudgetsStackElementModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 1
    }

    @Serializable
    sealed interface Skeleton {

        @Serializable
        @SerialName("budgets")
        data class Budgets(
            val skeleton: BudgetsListModel.Skeleton = BudgetsListModel.Skeleton(),
        ) : Skeleton

        @Serializable
        @SerialName("budget")
        data class Budget(
            val skeleton: LoadBudgetModel.Skeleton,
        ) : Skeleton
    }
}
