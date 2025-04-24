package hnau.pinfin.projector.budgetsorbudget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.state.StateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.budgetsorbudget.BudgetsOrBudgetModel
import hnau.pinfin.model.budgetsorbudget.BudgetsOrBudgetStateModel
import hnau.pinfin.projector.LoadBudgetProjector
import hnau.pinfin.projector.budgetslist.BudgetsProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class BudgetsOrBudgetProjector(
    scope: CoroutineScope,
    model: BudgetsOrBudgetModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun budgets(): BudgetsProjector.Dependencies

        fun budget(): LoadBudgetProjector.Dependencies
    }

    private val state: StateFlow<BudgetsOrBudgetElementProjector> = model
        .state
        .mapWithScope(scope) { stateScope, state ->
            when (state) {
                is BudgetsOrBudgetStateModel.Budgets -> BudgetsOrBudgetElementProjector.Budgets(
                    projector = BudgetsProjector(
                        scope = stateScope,
                        dependencies = dependencies.budgets(),
                        model = state.model,
                    )
                )

                is BudgetsOrBudgetStateModel.Budget -> BudgetsOrBudgetElementProjector.Budget(
                    projector = LoadBudgetProjector(
                        scope = stateScope,
                        dependencies = dependencies.budget(),
                        model = state.model,
                    )
                )
            }
        }

    @Composable
    fun Content() {
        state
            .collectAsState()
            .value
            .StateContent(
                modifier = Modifier.fillMaxSize(),
                label = "BudgetsOrBudget",
                transitionSpec = TransitionSpec.crossfade(),
                contentKey = BudgetsOrBudgetElementProjector::key,
            ) { elementProjector ->
                elementProjector.Content()
            }
    }
}