package org.hnau.pinfin.projector.budgetstack

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.stack.Content
import org.hnau.commons.app.projector.stack.StackProjectorTail
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.pinfin.model.budgetstack.BudgetStackElementModel
import org.hnau.pinfin.model.budgetstack.BudgetStackModel
import org.hnau.pinfin.model.budgetstack.fold
import org.hnau.pinfin.projector.CategoriesProjector
import org.hnau.pinfin.projector.accountstack.AccountStackProjector
import org.hnau.pinfin.projector.budget.BudgetProjector
import org.hnau.pinfin.projector.budget.transactions.TransactionsProjector
import org.hnau.pinfin.projector.categorystack.CategoryStackProjector
import org.hnau.pinfin.projector.transaction.TransactionProjector

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
    fun Content(
        contentPadding: PaddingValues,
    ) {
        tail.Content { elementProjector ->
            elementProjector.fold(
                ifBudget = {
                    it.Content(
                        contentPadding = contentPadding,
                    )
                },
                ifTransaction = {
                    it.Content(
                        contentPadding = contentPadding,
                    )
                },
                ifTransactions = {
                    it.Content(
                        contentPadding = contentPadding,
                        showAddButton = false,
                    )
                },
                ifAccount = {
                    it.Content(
                        contentPadding = contentPadding,
                    )
                },
                ifCategories = {
                    it.Content(
                        contentPadding = contentPadding,
                    )
                },
                ifCategory = {
                    it.Content(
                        contentPadding = contentPadding,
                    )
                },
            )
        }
    }
}
