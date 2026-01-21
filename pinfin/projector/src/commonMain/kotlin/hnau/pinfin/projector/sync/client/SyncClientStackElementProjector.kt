package hnau.pinfin.projector.sync.client

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.sync.client.budget.SyncClientLoadBudgetProjector
import hnau.pinfin.projector.sync.client.list.SyncClientListProjector

sealed interface SyncClientStackElementProjector {

    @Composable
    fun Content()

    val key: Int

    data class List(
        private val projector: SyncClientListProjector,
    ) : SyncClientStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }

    data class Budget(
        private val projector: SyncClientLoadBudgetProjector,
    ) : SyncClientStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 1
    }
}
