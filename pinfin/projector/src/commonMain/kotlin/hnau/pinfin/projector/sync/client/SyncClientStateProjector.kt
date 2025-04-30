package hnau.pinfin.projector.sync.client

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.sync.SyncStackProjector
import hnau.pinfin.projector.manage.ManageProjector
import hnau.pinfin.projector.sync.client.budget.SyncClientBudgetProjector
import hnau.pinfin.projector.sync.client.budget.SyncClientLoadBudgetProjector
import hnau.pinfin.projector.sync.client.list.SyncClientListProjector

sealed interface SyncClientStateProjector {

    @Composable
    fun Content()

    val key: Int

    data class List(
        private val projector: SyncClientListProjector,
    ) : SyncClientStateProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }

    data class Budget(
        private val projector: SyncClientLoadBudgetProjector,
    ) : SyncClientStateProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 1
    }
}