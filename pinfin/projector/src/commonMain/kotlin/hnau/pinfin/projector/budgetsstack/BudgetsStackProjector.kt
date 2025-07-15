package hnau.pinfin.projector.budgetsstack

import androidx.compose.runtime.Composable
import hnau.common.app.projector.stack.Content
import hnau.common.app.projector.stack.StackProjectorTail
import hnau.pinfin.model.budgetsstack.BudgetsStackElementModel
import hnau.pinfin.model.budgetsstack.BudgetsStackModel
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

    private val tail: StateFlow<StackProjectorTail<Int, BudgetsStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = { model -> model.key },
            createProjector = { scope, model ->
                when (model) {
                    is BudgetsStackElementModel.List -> BudgetsStackElementProjector.List(
                        BudgetsListProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.list(),
                        )
                    )

                    is BudgetsStackElementModel.Sync -> BudgetsStackElementProjector.Sync(
                        SyncStackProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.sync(),
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