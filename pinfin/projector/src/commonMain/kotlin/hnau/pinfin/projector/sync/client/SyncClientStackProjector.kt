package hnau.pinfin.projector.sync.client

import androidx.compose.runtime.Composable
import hnau.common.app.projector.stack.Content
import hnau.common.app.projector.stack.StackProjectorTail
import hnau.common.gen.sealup.annotations.SealUp
import hnau.common.gen.sealup.annotations.Variant
import hnau.pinfin.model.sync.client.SyncClientStackElementModel
import hnau.pinfin.model.sync.client.SyncClientStackModel
import hnau.pinfin.model.sync.client.fold
import hnau.pinfin.projector.sync.client.budget.SyncClientLoadBudgetProjector
import hnau.pinfin.projector.sync.client.list.SyncClientListProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class SyncClientStackProjector(
    scope: CoroutineScope,
    model: SyncClientStackModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun list(): SyncClientListProjector.Dependencies

        fun budget(): SyncClientLoadBudgetProjector.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = SyncClientListProjector::class,
                identifier = "list",
            ),
            Variant(
                type = SyncClientLoadBudgetProjector::class,
                identifier = "budget",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "SyncClientStackElementProjector",
    )
    interface PageProjector {

        @Composable
        fun Content()

        companion object
    }

    private val tail: StateFlow<StackProjectorTail<Int, SyncClientStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = SyncClientStackElementModel::ordinal,
            createProjector = { scope, model ->
                model.fold(
                    ifList = { listModel ->
                        PageProjector.list(
                            scope = scope,
                            model = listModel,
                            dependencies = dependencies.list(),
                        )
                    },
                    ifBudget = { budgetModel ->
                        PageProjector.budget(
                            scope = scope,
                            model = budgetModel,
                            dependencies = dependencies.budget(),
                        )
                    },
                )
            }
        )

    @Composable
    fun Content() {
        tail.Content { elementProjector ->
            elementProjector.Content()
        }
    }
}
