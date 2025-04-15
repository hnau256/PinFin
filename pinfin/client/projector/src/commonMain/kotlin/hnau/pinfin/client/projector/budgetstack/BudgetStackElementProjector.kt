package hnau.pinfin.client.projector.budgetstack

import androidx.compose.runtime.Composable
import hnau.pinfin.client.projector.transactions.TransactionsProjector
import hnau.pinfin.client.projector.transaction.TransactionProjector

sealed interface BudgetStackElementProjector {

    @Composable
    fun Content()

    val key: Int

    data class Main(
        private val projector: TransactionsProjector,
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
}