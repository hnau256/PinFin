package hnau.pinfin.projector.budgetsorbudget

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.budgetslist.BudgetsProjector
import hnau.pinfin.projector.LoadBudgetProjector

sealed interface BudgetsOrBudgetElementProjector {

    @Composable
    fun Content()

    val key: Int

    data class Budgets(
        private val projector: BudgetsProjector,
    ) : BudgetsOrBudgetElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }

    data class Budget(
        private val projector: LoadBudgetProjector,
    ) : BudgetsOrBudgetElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 1
    }
}