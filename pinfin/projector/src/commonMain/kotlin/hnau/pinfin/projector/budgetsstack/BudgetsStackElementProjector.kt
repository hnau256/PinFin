package hnau.pinfin.projector.budgetsstack

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.budgetslist.BudgetsListProjector
import hnau.pinfin.projector.LoadBudgetProjector

sealed interface BudgetsStackElementProjector {

    @Composable
    fun Content()

    val key: Int

    data class Budgets(
        private val projector: BudgetsListProjector,
    ) : BudgetsStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }

    data class Budget(
        private val projector: LoadBudgetProjector,
    ) : BudgetsStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 1
    }
}