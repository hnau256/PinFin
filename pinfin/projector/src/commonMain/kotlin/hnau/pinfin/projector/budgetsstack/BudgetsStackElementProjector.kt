package hnau.pinfin.projector.budgetsstack

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.budgetslist.BudgetsListProjector
import hnau.pinfin.projector.sync.SyncStackProjector

sealed interface BudgetsStackElementProjector {

    @Composable
    fun Content()

    val key: Int

    data class List(
        private val projector: BudgetsListProjector,
    ) : BudgetsStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }

    data class Sync(
        private val projector: SyncStackProjector,
    ) : BudgetsStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 1
    }
}
