package hnau.pinfin.client.projector.budgetstack

import androidx.compose.runtime.Composable
import hnau.common.compose.projector.stack.Content
import hnau.common.compose.projector.stack.StackProjectorTail
import hnau.pinfin.client.data.budget.AccountInfoResolver
import hnau.pinfin.client.data.budget.CategoryInfoResolver
import hnau.pinfin.client.model.budgetstack.BudgetStackElementModel
import hnau.pinfin.client.model.budgetstack.BudgetStackModel
import hnau.pinfin.client.projector.transactions.TransactionsProjector
import hnau.pinfin.client.projector.transaction.TransactionProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class BudgetStackProjector(
    private val scope: CoroutineScope,
    private val model: BudgetStackModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        @Shuffle
        interface WithInfo {

            fun main(): TransactionsProjector.Dependencies

            fun transaction(): TransactionProjector.Dependencies
        }

        fun withInfo(
            accountInfoResolver: AccountInfoResolver,
            categoryInfoResolver: CategoryInfoResolver,
        ): WithInfo
    }

    private val withInfoDependencies = dependencies.withInfo(
        accountInfoResolver = model.budgetRepository.account,
        categoryInfoResolver = model.budgetRepository.category,
    )

    private val tail: StateFlow<StackProjectorTail<Int, BudgetStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = { model -> model.key },
            createProjector = { scope, model ->
                when (model) {
                    is BudgetStackElementModel.Transactions -> BudgetStackElementProjector.Main(
                        TransactionsProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = withInfoDependencies.main(),
                        )
                    )

                    is BudgetStackElementModel.Transaction -> BudgetStackElementProjector.Transaction(
                        TransactionProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = withInfoDependencies.transaction(),
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