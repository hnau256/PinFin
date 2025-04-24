package hnau.pinfin.projector.manage

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.budgetslist.BudgetsListProjector
import hnau.pinfin.projector.LoadBudgetProjector

sealed interface ManageElementProjector {

    @Composable
    fun Content()

    val key: Int

    data class BudgetsList(
        private val projector: BudgetsListProjector,
    ) : ManageElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }

    data class Budget(
        private val projector: LoadBudgetProjector,
    ) : ManageElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 1
    }
}