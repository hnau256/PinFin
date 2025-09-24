package hnau.pinfin.projector.budget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import hnau.pinfin.projector.budget.analytics.AnalyticsProjector
import hnau.pinfin.projector.budget.config.BudgetConfigProjector
import hnau.pinfin.projector.budget.transactions.TransactionsProjector

sealed interface BudgetPageProjector {

    @Composable
    fun Content(
        bottomInset: Dp,
    )

    data class Transactions(
        val projector: TransactionsProjector,
    ) : BudgetPageProjector {

        @Composable
        override fun Content(
            bottomInset: Dp,
        ) {
            projector.Content(
                bottomInset = bottomInset,
            )
        }
    }

    data class Analytics(
        val projector: AnalyticsProjector,
    ) : BudgetPageProjector {

        @Composable
        override fun Content(
            bottomInset: Dp,
        ) {
            projector.Content(
                bottomInset = bottomInset,
            )
        }
    }

    data class Config(
        val projector: BudgetConfigProjector,
    ) : BudgetPageProjector {

        @Composable
        override fun Content(
            bottomInset: Dp,
        ) {
            projector.Content(
                bottomInset = bottomInset,
            )
        }
    }
}