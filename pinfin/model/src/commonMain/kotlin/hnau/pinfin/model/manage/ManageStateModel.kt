package hnau.pinfin.model.manage

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.model.budgetsstack.BudgetsStackModel
import hnau.pinfin.model.budgetstack.BudgetStackModel
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

    data class BudgetStack(
        val model: BudgetStackModel,
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
        @SerialName("budget_stack")
        data class BudgetStack(
            val skeleton: BudgetStackModel.Skeleton,
        ) : Skeleton
    }
}