package hnau.pinfin.projector.bidget

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import hnau.pinfin.projector.bidget.transactions.TransactionsProjector

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

    data class Analytics(
        val projector: AnalyticsProjector,
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

    data class Config(
        val projector: BudgetConfigProjector,
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