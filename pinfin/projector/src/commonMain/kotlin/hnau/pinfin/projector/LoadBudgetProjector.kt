package hnau.pinfin.projector

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.state.LoadableContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.LoadBudgetModel
import hnau.pinfin.projector.budgetstack.BudgetStackProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class LoadBudgetProjector(
    scope: CoroutineScope,
    model: LoadBudgetModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun mainStack(): BudgetStackProjector.Dependencies
    }

    private val mainSackProjector: StateFlow<Loadable<BudgetStackProjector>> = model
        .budgetStackModel
        .mapWithScope(scope) { scope, mainStackOrLoading ->
            mainStackOrLoading.map { budgetStack ->
                BudgetStackProjector(
                    scope = scope,
                    model = budgetStack,
                    dependencies = dependencies.mainStack()
                )
            }
        }

    @Composable
    fun Content() {
        mainSackProjector
            .collectAsState()
            .value
            .LoadableContent(
                modifier = Modifier.fillMaxSize(),
                transitionSpec = TransitionSpec.horizontal(),
            ) { mainStackProjector ->
                mainStackProjector.Content()
            }
    }
}