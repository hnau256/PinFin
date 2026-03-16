package org.hnau.pinfin.projector.budgetsstack

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.stack.Content
import org.hnau.commons.app.projector.stack.StackProjectorTail
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.pinfin.model.budgetsstack.BudgetsStackElementModel
import org.hnau.pinfin.model.budgetsstack.BudgetsStackModel
import org.hnau.pinfin.model.budgetsstack.fold
import org.hnau.pinfin.projector.budgetslist.BudgetsListProjector
import org.hnau.pinfin.projector.sync.SyncStackProjector

class BudgetsStackProjector(
    private val scope: CoroutineScope,
    private val model: BudgetsStackModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun list(): BudgetsListProjector.Dependencies

        fun sync(): SyncStackProjector.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = BudgetsListProjector::class,
                identifier = "list",
            ),
            Variant(
                type = SyncStackProjector::class,
                identifier = "sync",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "BudgetsStackElementProjector",
    )
    interface PageProjector {

        @Composable
        fun Content()

        companion object
    }

    private val tail: StateFlow<StackProjectorTail<Int, BudgetsStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = BudgetsStackElementModel::ordinal,
            createProjector = { scope, model ->
                model.fold(
                    ifList = { listModel ->
                        PageProjector.list(
                            scope = scope,
                            model = listModel,
                            dependencies = dependencies.list(),
                        )
                    },
                    ifSync = { syncModel ->
                        PageProjector.sync(
                            scope = scope,
                            model = syncModel,
                            dependencies = dependencies.sync(),
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
