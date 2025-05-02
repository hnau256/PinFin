package hnau.pinfin.projector.manage

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.LoadBudgetProjector
import hnau.pinfin.projector.budgetsstack.BudgetsStackProjector

sealed interface ManageElementProjector {

    @Composable
    fun Content()

    val key: Int

    data class BudgetsStack(
        private val projector: BudgetsStackProjector,
    ) : ManageElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }

    data class LoadBudget(
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