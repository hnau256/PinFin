package hnau.pinfin.projector.budget

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import hnau.pinfin.projector.budget.analytics.AnalyticsProjector
import hnau.pinfin.projector.budget.config.BudgetConfigProjector
import hnau.pinfin.projector.budget.transactions.TransactionsProjector

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