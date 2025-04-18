package hnau.pinfin.client.projector.bidget

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import hnau.pinfin.client.projector.bidget.transactions.TransactionsProjector

sealed interface BudgetPageProjector {

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    )

    data class Transactions(
        val projector: TransactionsProjector,
    ) : BudgetPageProjector {

        @Composable
        override fun Content(
            contentPadding: PaddingValues,
        ) {
            projector.Content(
                contentPadding = contentPadding,
            )
        }
    }
}