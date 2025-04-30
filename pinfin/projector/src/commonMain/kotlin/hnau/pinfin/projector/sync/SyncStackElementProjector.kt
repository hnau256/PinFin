package hnau.pinfin.projector.sync

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.sync.client.SyncClientStackProjector

sealed interface SyncStackElementProjector {

    @Composable
    fun Content()

    val key: Int

    data class Start(
        private val projector: StartSyncProjector,
    ) : SyncStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }

    data class Client(
        private val projector: SyncClientStackProjector,
    ) : SyncStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 1
    }

    data class Server(
        private val projector: SyncServerProjector,
    ) : SyncStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 2
    }
}