package hnau.pinfin.projector.budgetsstack

import androidx.compose.runtime.Composable
import hnau.common.compose.projector.stack.Content
import hnau.common.compose.projector.stack.StackProjectorTail
import hnau.pinfin.model.budgetsstack.BudgetsStackElementModel
import hnau.pinfin.model.budgetsstack.BudgetsStackModel
import hnau.pinfin.projector.budgetslist.BudgetsListProjector
import hnau.pinfin.projector.LoadBudgetProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class BudgetsStackProjector(
    private val scope: CoroutineScope,
    private val model: BudgetsStackModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun budgets(): BudgetsListProjector.Dependencies

        fun budget(): LoadBudgetProjector.Dependencies
    }

    private val tail: StateFlow<StackProjectorTail<Int, BudgetsStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = { model -> model.key },
            createProjector = { scope, model ->
                when (model) {
                    is BudgetsStackElementModel.Budgets -> BudgetsStackElementProjector.Budgets(
                        BudgetsListProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.budgets(),
                        )
                    )

                    is BudgetsStackElementModel.Budget -> BudgetsStackElementProjector.Budget(
                        LoadBudgetProjector(
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