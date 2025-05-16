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
import hnau.pinfin.projector.IconProjector
import hnau.pinfin.projector.budgetsstack.BudgetsStackProjector
import hnau.pinfin.projector.budgetstack.BudgetStackProjector
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

        fun budgetsStack(): BudgetsStackProjector.Dependencies

        fun budgetStack(): BudgetStackProjector.Dependencies

        fun icon(): IconProjector.Dependencies
    }

    private val state: StateFlow<ManageElementProjector> = model
        .state
        .mapWithScope(scope) { stateScope, state ->
            when (state) {
                is ManageStateModel.BudgetsStack -> ManageElementProjector.BudgetsStack(
                    projector = BudgetsStackProjector(
                        scope = stateScope,
                        dependencies = dependencies.budgetsStack(),
                        model = state.model,
                    )
                )

                is ManageStateModel.BudgetStack -> ManageElementProjector.BudgetStack(
                    projector = BudgetStackProjector(
                        scope = stateScope,
                        dependencies = dependencies.budgetStack(),
                        model = state.model,
                    )
                )
            }
        }

    private val icon = IconProjector(
        scope = scope,
        dependencies = dependencies.icon(),
        model = model.icon,
    )

    @Composable
    fun Content() {
        icon.Content()
        /*state
            .collectAsState()
            .value
            .StateContent(
                modifier = Modifier.fillMaxSize(),
                label = "Manage",
                transitionSpec = TransitionSpec.crossfade(),
                contentKey = ManageElementProjector::key,
            ) { elementProjector ->
                elementProjector.Content()
            }*/
    }
}