package hnau.pinfin.projector.manage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.state.StateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.manage.ManageModel
import hnau.pinfin.model.manage.ManageStateModel
import hnau.pinfin.projector.LoadBudgetProjector
import hnau.pinfin.projector.budgetslist.BudgetsListProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class ManageProjector(
    scope: CoroutineScope,
    model: ManageModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun budgetsList(): BudgetsListProjector.Dependencies

        fun budget(): LoadBudgetProjector.Dependencies
    }

    private val state: StateFlow<ManageElementProjector> = model
        .state
        .mapWithScope(scope) { stateScope, state ->
            when (state) {
                is ManageStateModel.BudgetsList -> ManageElementProjector.BudgetsList(
                    projector = BudgetsListProjector(
                        scope = stateScope,
                        dependencies = dependencies.budgetsList(),
                        model = state.model,
                    )
                )

                is ManageStateModel.Budget -> ManageElementProjector.Budget(
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
                label = "Manage",
                transitionSpec = TransitionSpec.crossfade(),
                contentKey = ManageElementProjector::key,
            ) { elementProjector ->
                elementProjector.Content()
            }
    }
}