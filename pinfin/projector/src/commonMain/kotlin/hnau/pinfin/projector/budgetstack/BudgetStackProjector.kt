package hnau.pinfin.projector.budgetstack

import androidx.compose.runtime.Composable
import hnau.common.app.projector.stack.Content
import hnau.common.app.projector.stack.StackProjectorTail
import hnau.pinfin.model.budgetstack.BudgetStackElementModel
import hnau.pinfin.model.budgetstack.BudgetStackModel
import hnau.pinfin.projector.CategoriesProjector
import hnau.pinfin.projector.accountstack.AccountStackProjector
import hnau.pinfin.projector.budget.BudgetProjector
import hnau.pinfin.projector.categorystack.CategoryStackProjector
import hnau.pinfin.projector.transaction.TransactionProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class BudgetStackProjector(
    private val scope: CoroutineScope,
    private val model: BudgetStackModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun budget(): BudgetProjector.Dependencies

        fun transaction(): TransactionProjector.Dependencies

        fun account(): AccountStackProjector.Dependencies

        fun categories(): CategoriesProjector.Dependencies

        fun category(): CategoryStackProjector.Dependencies
    }

    private val tail: StateFlow<StackProjectorTail<Int, BudgetStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = { model -> model.key },
            createProjector = { scope, model ->
                when (model) {
                    is BudgetStackElementModel.Budget -> BudgetStackElementProjector.Budget(
                        BudgetProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.budget(),
                        )
                    )

                    is BudgetStackElementModel.Transaction -> BudgetStackElementProjector.Transaction(
                        TransactionProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.transaction(),
                        )
                    )

                    is BudgetStackElementModel.Account -> BudgetStackElementProjector.Account(
                        AccountStackProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.account(),
                        )
                    )

                    is BudgetStackElementModel.Categories -> BudgetStackElementProjector.Categories(
                        CategoriesProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.categories(),
                        )
                    )

                    is BudgetStackElementModel.Category -> BudgetStackElementProjector.Category(
                        CategoryStackProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.category(),
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