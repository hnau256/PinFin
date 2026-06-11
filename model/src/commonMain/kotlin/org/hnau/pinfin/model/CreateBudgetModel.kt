package org.hnau.pinfin.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.manage.BudgetOpener
import org.hnau.pinfin.model.utils.budget.repository.demo.DemoBudget
import org.hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import org.hnau.pinfin.model.utils.budget.storage.createNewBudgetIfNotExistsAndGet

class CreateBudgetModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val budgetsStorage: BudgetsStorage

        val budgetOpener: BudgetOpener
    }

    @Serializable
    data class Skeleton(
        val a: Int = 0,
    )

    val createNewBudget: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>> =
        actionOrCancelIfExecuting(
            scope = scope,
        ) {
            val id = BudgetId.new()
            dependencies.budgetsStorage.createNewBudgetIfNotExists(
                id = id,
            )
            dependencies.budgetOpener.openBudget(
                budgetId = id,
            )
        }

    val createDemoBudget: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>> =
        actionOrCancelIfExecuting(
            scope = scope,
        ) {
            val updates = withContext(Dispatchers.Default) {
                DemoBudget.updates
            }
            dependencies
                .budgetsStorage
                .createNewBudgetIfNotExistsAndGet(
                    id = BudgetId.new(),
                )
                .applyUpdates(updates)
        }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}