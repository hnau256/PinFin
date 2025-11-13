package hnau.pinfin.projector.budgetstack

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import hnau.pinfin.projector.CategoriesProjector
import hnau.pinfin.projector.accountstack.AccountStackProjector
import hnau.pinfin.projector.budget.BudgetProjector
import hnau.pinfin.projector.budget.transactions.TransactionsProjector
import hnau.pinfin.projector.categorystack.CategoryStackProjector
import hnau.pinfin.projector.transaction.TransactionProjector

sealed interface BudgetStackElementProjector {

    @Composable
    fun Content()

    val key: Int

    data class Budget(
        private val projector: BudgetProjector,
    ) : BudgetStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }

    data class Transaction(
        private val projector: TransactionProjector,
    ) : BudgetStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 1
    }

    data class Transactions(
        private val projector: TransactionsProjector,
    ) : BudgetStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content(
                bottomInset = WindowInsets
                    .navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding(),
                showAddButton = false,
            )
        }

        override val key: Int
            get() = 2
    }

    data class Account(
        private val projector: AccountStackProjector,
    ) : BudgetStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 3
    }

    data class Categories(
        private val projector: CategoriesProjector,
    ) : BudgetStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 4
    }

    data class Category(
        private val projector: CategoryStackProjector,
    ) : BudgetStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 5
    }
}