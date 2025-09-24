package hnau.pinfin.model.manage

import hnau.common.app.model.goback.GoBackHandler
import hnau.pinfin.model.budgetsstack.BudgetsStackModel
import hnau.pinfin.model.budgetstack.BudgetStackModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ManageStateModel {

    val id: Int

    val goBackHandler: GoBackHandler

    data class BudgetsStack(
        val model: BudgetsStackModel,
    ): ManageStateModel {

        override val id: Int
            get() = 0

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }

    data class BudgetStack(
        val model: BudgetStackModel,
    ):  ManageStateModel {

        override val id: Int
            get() = 1

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
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