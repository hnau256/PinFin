package hnau.pinfin.projector.sync.client

import androidx.compose.runtime.Composable
import hnau.common.projector.stack.Content
import hnau.common.projector.stack.StackProjectorTail
import hnau.pinfin.model.sync.client.SyncClientStackElementModel
import hnau.pinfin.model.sync.client.SyncClientStackModel
import hnau.pinfin.projector.sync.client.budget.SyncClientLoadBudgetProjector
import hnau.pinfin.projector.sync.client.list.SyncClientListProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class SyncClientStackProjector(
    scope: CoroutineScope,
    model: SyncClientStackModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun list(): SyncClientListProjector.Dependencies

        fun budget(): SyncClientLoadBudgetProjector.Dependencies
    }

    private val tail: StateFlow<StackProjectorTail<Int, SyncClientStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = { model -> model.key },
            createProjector = { scope, model ->
                when (model) {
                    is SyncClientStackElementModel.List -> SyncClientStackElementProjector.List(
                        SyncClientListProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.list(),
                        )
                    )

                    is SyncClientStackElementModel.Budget -> SyncClientStackElementProjector.Budget(
                        SyncClientLoadBudgetProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.budget(),
                        )
                    )
                }
            }
        )

    @Composable
    fun Content() {
        tail.Content { elementProjector ->
            elementProjector.Content()
        }
    }
}