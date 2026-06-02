package org.hnau.pinfin.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.actionOrInProgressIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.manage.BudgetOpener
import org.hnau.pinfin.model.utils.budget.storage.BudgetsStorage

class BudgetSwitchModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val id: BudgetId

        val budgetsStorage: BudgetsStorage

        val budgetOpener: BudgetOpener
    }

    data class Item(
        val title: StateFlow<String>,
        val state: State,
    ) {

        sealed interface State {

            data object Selected : State

            data class NotSelected(
                val select: StateFlow<ActionOrElse<Unit, CancelOrInProgress.InProgress>>,
            ) : State
        }
    }

    val items: StateFlow<List<Item>> = dependencies
        .budgetsStorage
        .list
        .mapWithScope(scope) { scope, budgets ->
            val thisId = dependencies.id
            budgets.map { (id, budget) ->
                Item(
                    title = budget.state.mapState(scope) { it.info.title },
                    state = when (id) {
                        thisId -> Item.State.Selected

                        else -> Item.State.NotSelected(
                            select = actionOrInProgressIfExecuting(scope) {
                                dependencies
                                    .budgetOpener
                                    .openBudget(thisId)
                            }
                        )
                    }
                )
            }
        }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}