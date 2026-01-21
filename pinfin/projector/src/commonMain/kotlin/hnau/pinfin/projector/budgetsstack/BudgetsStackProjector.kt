package hnau.pinfin.projector.budgetsstack

import androidx.compose.runtime.Composable
import hnau.common.app.projector.stack.Content
import hnau.common.app.projector.stack.StackProjectorTail
import hnau.common.gen.sealup.annotations.SealUp
import hnau.common.gen.sealup.annotations.Variant
import hnau.pinfin.model.budgetsstack.BudgetsStackElementModel
import hnau.pinfin.model.budgetsstack.BudgetsStackModel
import hnau.pinfin.model.budgetsstack.fold
import hnau.pinfin.projector.budgetslist.BudgetsListProjector
import hnau.pinfin.projector.sync.SyncStackProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

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
