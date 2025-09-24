package hnau.pinfin.model.sync

import hnau.common.app.model.goback.GoBackHandler
import hnau.pinfin.model.sync.client.SyncClientStackModel
import hnau.pinfin.model.sync.server.SyncServerModel
import hnau.pinfin.model.sync.start.StartSyncModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface SyncStackElementModel {

    val key: Int

    val goBackHandler: GoBackHandler

    data class Start(
        val model: StartSyncModel,
    ): SyncStackElementModel {

        override val key: Int
            get() = 0

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }

    data class Client(
        val model: SyncClientStackModel,
    ): SyncStackElementModel {

        override val key: Int
            get() = 1

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }

    data class Server(
        val model: SyncServerModel,
    ): SyncStackElementModel {

        override val key: Int
            get() = 2

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }

    @Serializable
    sealed interface Skeleton {

        val key: Int

        @Serializable
        @SerialName("start")
        data class Start(
            val skeleton: StartSyncModel.Skeleton = StartSyncModel.Skeleton(),
        ) : Skeleton {

            override val key: Int
                get() = 0
        }

        @Serializable
        @SerialName("client")
        data class Client(
            val skeleton: SyncClientStackModel.Skeleton,
        ) : Skeleton {

            override val key: Int
                get() = 1
        }

        @Serializable
        @SerialName("server")
        data class Server(
            val skeleton: SyncServerModel.Skeleton,
        ) : Skeleton {

            override val key: Int
                get() = 2
        }
    }
}