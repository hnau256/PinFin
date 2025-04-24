package hnau.pinfin.projector.budgetsorsync

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.SyncProjector
import hnau.pinfin.projector.budgetsorbudget.BudgetsOrBudgetProjector

sealed interface BudgetsOrSyncElementProjector {

    @Composable
    fun Content()

    val key: Int

    data class Budgets(
        private val projector: BudgetsOrBudgetProjector,
    ) : BudgetsOrSyncElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }

    data class Sync(
        private val projector: SyncProjector,
    ) : BudgetsOrSyncElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 1
    }
}