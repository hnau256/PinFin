package hnau.pinfin.sync.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
sealed interface SyncHandle<O> {

    val responseSerializer: KSerializer<O>

    @Serializable
    @SerialName("ping")
    data object Ping : SyncHandle<Ping.Response> {

        override val responseSerializer: KSerializer<Response>
            get() = Response.serializer()

        @Serializable
        data object Response
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        val serializer: KSerializer<SyncHandle<*>> = serializer(
            typeSerial0 = Unit.serializer(),
        ) as KSerializer<SyncHandle<*>>
    }
}