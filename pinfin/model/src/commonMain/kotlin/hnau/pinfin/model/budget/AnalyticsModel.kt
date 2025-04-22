package hnau.pinfin.model.budget

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.data.budget.BudgetRepository
import hnau.pinfin.data.budget.BudgetState
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class AnalyticsModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val budgetsRepository: BudgetRepository
    }

    @Serializable
    /*data*/ class Skeleton

    val budgetState: StateFlow<BudgetState>
        get() = dependencies
            .budgetsRepository
            .state
}