package hnau.pinfin.projector.budgetsorsync

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.state.StateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.budgetsorsync.BudgetsOrSyncModel
import hnau.pinfin.model.budgetsorsync.BudgetsOrSyncStateModel
import hnau.pinfin.projector.SyncProjector
import hnau.pinfin.projector.budgetsorbudget.BudgetsOrBudgetProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class BudgetsOrSyncProjector(
    scope: CoroutineScope,
    model: BudgetsOrSyncModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun budgets(): BudgetsOrBudgetProjector.Dependencies

        fun sync(): SyncProjector.Dependencies
    }

    private val state: StateFlow<BudgetsOrSyncElementProjector> = model
        .state
        .mapWithScope(scope) { stateScope, state ->
            when (state) {
                is BudgetsOrSyncStateModel.Budgets -> BudgetsOrSyncElementProjector.Budgets(
                    projector = BudgetsOrBudgetProjector(
                        scope = stateScope,
                        dependencies = dependencies.budgets(),
                        model = state.model,
                    )
                )

                is BudgetsOrSyncStateModel.Sync -> BudgetsOrSyncElementProjector.Sync(
                    projector = SyncProjector(
                        scope = stateScope,
                        dependencies = dependencies.sync(),
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
                label = "BudgetsOrSync",
                transitionSpec = TransitionSpec.crossfade(),
                contentKey = BudgetsOrSyncElementProjector::key,
            ) { elementProjector ->
                elementProjector.Content()
            }
    }
}