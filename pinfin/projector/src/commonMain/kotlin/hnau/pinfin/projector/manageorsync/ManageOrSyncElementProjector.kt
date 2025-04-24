package hnau.pinfin.projector.manageorsync

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.SyncProjector
import hnau.pinfin.projector.manage.ManageProjector

sealed interface ManageOrSyncElementProjector {

    @Composable
    fun Content()

    val key: Int

    data class Manage(
        private val projector: ManageProjector,
    ) : ManageOrSyncElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }

    data class Sync(
        private val projector: SyncProjector,
    ) : ManageOrSyncElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 1
    }
}