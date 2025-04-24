package hnau.pinfin.model.manage

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.model.budgets.BudgetsListModel
import hnau.pinfin.model.LoadBudgetModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ManageStateModel : GoBackHandlerProvider {

    val id: Int

    data class BudgetsList(
        val model: BudgetsListModel,
    ) : GoBackHandlerProvider by model, ManageStateModel {

        override val id: Int
            get() = 0
    }

    data class Budget(
        val model: LoadBudgetModel,
    ) : GoBackHandlerProvider by model, ManageStateModel {

        override val id: Int
            get() = 1
    }

    @Serializable
    sealed interface Skeleton {

        @Serializable
        @SerialName("budgets_list")
        data class BudgetsList(
            val skeleton: BudgetsListModel.Skeleton,
        ) : Skeleton

        @Serializable
        @SerialName("budget")
        data class Budget(
            val skeleton: LoadBudgetModel.Skeleton,
        ) : Skeleton
    }
}