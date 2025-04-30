package hnau.pinfin.projector.sync.client

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.state.StateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.sync.client.SyncClientModel
import hnau.pinfin.model.sync.client.SyncClientStateModel
import hnau.pinfin.projector.sync.client.budget.SyncClientLoadBudgetProjector
import hnau.pinfin.projector.sync.client.list.SyncClientListProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class SyncClientProjector(
    scope: CoroutineScope,
    model: SyncClientModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun list(): SyncClientListProjector.Dependencies

        fun budget(): SyncClientLoadBudgetProjector.Dependencies
    }

    private val state: StateFlow<SyncClientStateProjector> = model
        .state
        .mapWithScope(scope) { stateScope, state ->
            when (state) {
                is SyncClientStateModel.List -> SyncClientStateProjector.List(
                    SyncClientListProjector(
                        scope = stateScope,
                        model = state.model,
                        dependencies = dependencies.list()
                    )
                )

                is SyncClientStateModel.Budget -> SyncClientStateProjector.Budget(
                    SyncClientLoadBudgetProjector(
                        scope = stateScope,
                        model = state.model,
                        dependencies = dependencies.budget()
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
                label = "SyncClientListOrBudget",
                transitionSpec = TransitionSpec.crossfade(),
                contentKey = SyncClientStateProjector::key,
            ) { state ->
                state.Content()
            }
    }
}