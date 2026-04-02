package org.hnau.pinfin.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.InProgressRegistry
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.repository.DemoBudget
import org.hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import org.hnau.pinfin.model.utils.budget.storage.createNewBudgetIfNotExistsAndGet
import org.hnau.upchain.core.repository.upchain.addUpdates

class NoBudgetsModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val budgetsStorage: BudgetsStorage
    }

    private val inProgressRegistry = InProgressRegistry(
        scope = scope,
    )

    val inProgress: StateFlow<Boolean>
        get() = inProgressRegistry.inProgress

    fun createNewBudget() {
        scope.launch {
            inProgressRegistry.executeRegistered {
                dependencies.budgetsStorage.createNewBudgetIfNotExists(
                    id = BudgetId.new(),
                )
            }
        }
    }

    fun createDemoBudget() {
        scope.launch {
            inProgressRegistry.executeRegistered {
                val updates = withContext(Dispatchers.Default) {
                    DemoBudget
                        .updates
                }
                dependencies
                    .budgetsStorage
                    .createNewBudgetIfNotExistsAndGet(DemoBudget.id)
                    .upchainRepository
                    .addUpdates(updates)
            }
        }
    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}