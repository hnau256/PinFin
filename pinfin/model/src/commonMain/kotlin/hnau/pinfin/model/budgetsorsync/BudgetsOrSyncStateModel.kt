package hnau.pinfin.model.budgetsorsync

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.model.budgetsorbudget.BudgetsOrBudgetModel
import hnau.pinfin.model.SyncModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface BudgetsOrSyncStateModel: GoBackHandlerProvider {

    val id: Int

    data class Budgets(
        val model: BudgetsOrBudgetModel,
    ): GoBackHandlerProvider by model, BudgetsOrSyncStateModel {

        override val id: Int
            get() = 0
    }

    data class Sync(
        val model: SyncModel,
    ): GoBackHandlerProvider by model, BudgetsOrSyncStateModel {

        override val id: Int
            get() = 1
    }

    @Serializable
    sealed interface Skeleton {

        @Serializable
        @SerialName("budgets")
        data class Budgets(
            val skeleton: BudgetsOrBudgetModel.Skeleton,
        ): Skeleton

        @Serializable
        @SerialName("sync")
        data class Sync(
            val skeleton: SyncModel.Skeleton,
        ): Skeleton
    }
}