package hnau.pinfin.model.sync

import hnau.common.model.goback.GoBackHandlerProvider
import hnau.pinfin.model.sync.client.SyncClientStackModel
import hnau.pinfin.model.sync.server.SyncServerModel
import hnau.pinfin.model.sync.start.StartSyncModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface SyncStackElementModel: GoBackHandlerProvider {

    val key: Int

    data class Start(
        val model: StartSyncModel,
    ): GoBackHandlerProvider by model, SyncStackElementModel {

        override val key: Int
            get() = 0
    }

    data class Client(
        val model: SyncClientStackModel,
    ): GoBackHandlerProvider by model, SyncStackElementModel {

        override val key: Int
            get() = 1
    }

    data class Server(
        val model: SyncServerModel,
    ): GoBackHandlerProvider by model, SyncStackElementModel {

        override val key: Int
            get() = 2
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