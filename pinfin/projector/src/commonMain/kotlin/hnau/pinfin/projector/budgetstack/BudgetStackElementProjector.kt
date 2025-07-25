package hnau.pinfin.projector.budgetstack

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.AccountProjector
import hnau.pinfin.projector.CategoriesProjector
import hnau.pinfin.projector.CategoryProjector
import hnau.pinfin.projector.budget.BudgetProjector
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

    data class Account(
        private val projector: AccountProjector,
    ) : BudgetStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 2
    }

    data class Categories(
        private val projector: CategoriesProjector,
    ) : BudgetStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 3
    }

    data class Category(
        private val projector: CategoryProjector,
    ) : BudgetStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 4
    }
}