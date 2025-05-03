package hnau.pinfin.projector.manage

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.budgetsstack.BudgetsStackProjector
import hnau.pinfin.projector.budgetstack.BudgetStackProjector

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

    data class BudgetStack(
        private val projector: BudgetStackProjector,
    ) : ManageElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 1
    }
}