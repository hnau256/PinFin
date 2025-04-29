package hnau.pinfin.projector.sync

import androidx.compose.runtime.Composable

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
        private val projector: SyncClientProjector,
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