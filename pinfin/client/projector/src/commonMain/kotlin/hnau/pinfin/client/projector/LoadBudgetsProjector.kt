package hnau.pinfin.client.projector

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.state.LoadableContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.client.model.LoadBudgetsModel
import hnau.pinfin.client.projector.budgetsstack.BudgetsStackProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class LoadBudgetsProjector(
    scope: CoroutineScope,
    model: LoadBudgetsModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun budgetsStack(): BudgetsStackProjector.Dependencies
    }

    private val budgetsSackProjector: StateFlow<Loadable<BudgetsStackProjector>> = model
        .budgetsStack
        .mapWithScope(scope) { scope, budgetsStackOrLoading ->
            budgetsStackOrLoading.map { budgetsStack ->
                BudgetsStackProjector(
                    scope = scope,
                    model = budgetsStack,
                    dependencies = dependencies.budgetsStack(),
                )
            }
        }

    @Composable
    fun Content() {
        budgetsSackProjector
            .collectAsState()
            .value
            .LoadableContent(
                modifier = Modifier.fillMaxSize(),
                transitionSpec = TransitionSpec.horizontal(),
            ) { budgetsStackProjector ->
                budgetsStackProjector.Content()
            }
    }
}