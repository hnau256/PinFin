package org.hnau.pinfin.model.budgetslist.item

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.InProgressRegistry
import org.hnau.commons.kotlin.coroutines.flow.state.mapStateLite
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.manage.BudgetOpener
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.BudgetInfo
import org.hnau.pinfin.model.utils.budget.state.BudgetState

class BudgetItemModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val id: BudgetId

        val repository: BudgetRepository

        val budgetOpener: BudgetOpener
    }


    private val inProgressRegistry = InProgressRegistry(
        scope = scope,
    )

    val info: StateFlow<BudgetInfo> = dependencies
        .repository
        .state
        .mapStateLite(BudgetState::info)

    val inProgress: StateFlow<Boolean>
        get() = inProgressRegistry.inProgress

    fun open() {
        scope.launch {
            inProgressRegistry.executeRegistered {
                dependencies.budgetOpener.openBudget(dependencies.id)
            }
        }
    }
}