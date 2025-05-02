package hnau.pinfin.model.manage

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.model.LoadBudgetModel
import hnau.pinfin.model.budgetsstack.BudgetsStackModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ManageStateModel : GoBackHandlerProvider {

    val id: Int

    data class BudgetsStack(
        val model: BudgetsStackModel,
    ) : GoBackHandlerProvider by model, ManageStateModel {

        override val id: Int
            get() = 0
    }

    data class LoadBudget(
        val model: LoadBudgetModel,
    ) : GoBackHandlerProvider by model, ManageStateModel {

        override val id: Int
            get() = 1
    }

    @Serializable
    sealed interface Skeleton {

        @Serializable
        @SerialName("budgets_stack")
        data class BudgetsStack(
            val skeleton: BudgetsStackModel.Skeleton,
        ) : Skeleton

        @Serializable
        @SerialName("load_budget")
        data class LoadBudget(
            val skeleton: LoadBudgetModel.Skeleton,
        ) : Skeleton
    }
}