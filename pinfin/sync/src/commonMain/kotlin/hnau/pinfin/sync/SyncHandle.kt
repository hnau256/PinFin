package hnau.pinfin.data.sync

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface SyncHandle<O, I : SyncHandle<O, I>> {

    val responseSerializer: KSerializer<O>
    val requestSerializer: KSerializer<I>

    @Serializable
    @SerialName("ping")
    data object Ping : SyncHandle<Ping.Response, Ping> {

        override val responseSerializer: KSerializer<Response>
            get() = Response.serializer()

        override val requestSerializer: KSerializer<Ping>
            get() = serializer()

        @Serializable
        data object Response
    }
}