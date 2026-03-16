package org.hnau.pinfin.projector.sync.client

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.stack.Content
import org.hnau.commons.app.projector.stack.StackProjectorTail
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.pinfin.model.sync.client.SyncClientStackElementModel
import org.hnau.pinfin.model.sync.client.SyncClientStackModel
import org.hnau.pinfin.model.sync.client.fold
import org.hnau.pinfin.projector.sync.client.budget.SyncClientLoadBudgetProjector
import org.hnau.pinfin.projector.sync.client.list.SyncClientListProjector

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
