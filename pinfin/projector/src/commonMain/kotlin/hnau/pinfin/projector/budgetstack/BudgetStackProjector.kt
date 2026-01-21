package hnau.pinfin.projector.budgetstack

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import hnau.common.app.projector.stack.Content
import hnau.common.app.projector.stack.StackProjectorTail
import hnau.common.gen.sealup.annotations.SealUp
import hnau.common.gen.sealup.annotations.Variant
import hnau.pinfin.model.budgetstack.BudgetStackElementModel
import hnau.pinfin.model.budgetstack.BudgetStackModel
import hnau.pinfin.model.budgetstack.fold
import hnau.pinfin.projector.CategoriesProjector
import hnau.pinfin.projector.accountstack.AccountStackProjector
import hnau.pinfin.projector.budget.BudgetProjector
import hnau.pinfin.projector.budget.transactions.TransactionsProjector
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

        fun transactions(): TransactionsProjector.Dependencies

        fun account(): AccountStackProjector.Dependencies

        fun categories(): CategoriesProjector.Dependencies

        fun category(): CategoryStackProjector.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = BudgetProjector::class,
                identifier = "budget",
            ),
            Variant(
                type = TransactionProjector::class,
                identifier = "transaction",
            ),
            Variant(
                type = TransactionsProjector::class,
                identifier = "transactions",
            ),
            Variant(
                type = AccountStackProjector::class,
                identifier = "account",
            ),
            Variant(
                type = CategoriesProjector::class,
                identifier = "categories",
            ),
            Variant(
                type = CategoryStackProjector::class,
                identifier = "category",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "BudgetStackElementProjector",
    )
    interface PageProjector {

        companion object
    }

    private val tail: StateFlow<StackProjectorTail<Int, BudgetStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = BudgetStackElementModel::ordinal,
            createProjector = { scope, model ->
                model.fold(
                    ifBudget = { budgetModel ->
                        PageProjector.budget(
                            scope = scope,
                            model = budgetModel,
                            dependencies = dependencies.budget(),
                        )
                    },
                    ifTransaction = { transactionModel ->
                        PageProjector.transaction(
                            scope = scope,
                            model = transactionModel,
                            dependencies = dependencies.transaction(),
                        )
                    },
                    ifTransactions = { transactionsModel ->
                        PageProjector.transactions(
                            scope = scope,
                            model = transactionsModel,
                            dependencies = dependencies.transactions(),
                        )
                    },
                    ifAccount = { accountModel ->
                        PageProjector.account(
                            scope = scope,
                            model = accountModel,
                            dependencies = dependencies.account(),
                        )
                    },
                    ifCategories = { categoriesModel ->
                        PageProjector.categories(
                            scope = scope,
                            model = categoriesModel,
                            dependencies = dependencies.categories(),
                        )
                    },
                    ifCategory = { categoryModel ->
                        PageProjector.category(
                            scope = scope,
                            model = categoryModel,
                            dependencies = dependencies.category(),
                        )
                    },
                )
            }
        )

    @Composable
    fun Content() {
        tail.Content { elementProjector ->
            elementProjector.fold(
                ifBudget = { it.Content() },
                ifTransaction = { it.Content() },
                ifTransactions = {
                    it.Content(
                        bottomInset = WindowInsets
                            .navigationBars
                            .asPaddingValues()
                            .calculateBottomPadding(),
                        showAddButton = false,
                    )
                },
                ifAccount = { it.Content() },
                ifCategories = { it.Content() },
                ifCategory = { it.Content() },
            )
        }
    }
}
