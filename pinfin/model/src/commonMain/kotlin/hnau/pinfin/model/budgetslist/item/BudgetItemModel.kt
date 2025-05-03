package hnau.pinfin.model.budgetslist.item

import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.InProgressRegistry
import hnau.common.kotlin.coroutines.mapStateLite
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.manage.BudgetOpener
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.BudgetInfo
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.utils.toBudgetInfoStateFlow
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class BudgetItemModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Shuffle
    interface Dependencies {

        val id: BudgetId

        val deferredRepository: BudgetRepository

        val budgetOpener: BudgetOpener
    }

    @Serializable
    /*data*/ class Skeleton

    private val inProgressRegistry = InProgressRegistry()

    val info: StateFlow<BudgetInfo> = dependencies
        .deferredRepository
        .state
        .mapStateLite(BudgetState::info)

    //TODO
    val inProgress: StateFlow<Boolean>
        get() = inProgressRegistry.isProgress

    fun open() {
        scope.launch {
            inProgressRegistry.executeRegistered {
                dependencies.budgetOpener.openBudget(dependencies.id)
            }
        }
    }
}