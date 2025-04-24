package hnau.pinfin.model.budgetsorbudget

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.model.budgets.BudgetsModel
import hnau.pinfin.model.LoadBudgetModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface BudgetsOrBudgetStateModel : GoBackHandlerProvider {

    val id: Int

    data class Budgets(
        val model: BudgetsModel,
    ) : GoBackHandlerProvider by model, BudgetsOrBudgetStateModel {

        override val id: Int
            get() = 0
    }

    data class Budget(
        val model: LoadBudgetModel,
    ) : GoBackHandlerProvider by model, BudgetsOrBudgetStateModel {

        override val id: Int
            get() = 1
    }

    @Serializable
    sealed interface Skeleton {

        @Serializable
        @SerialName("budgets")
        data class Budgets(
            val skeleton: BudgetsModel.Skeleton,
        ) : Skeleton

        @Serializable
        @SerialName("budget")
        data class Budget(
            val skeleton: LoadBudgetModel.Skeleton,
        ) : Skeleton
    }
}