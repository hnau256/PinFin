package hnau.pinfin.model.sync.utils

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class ServerPort(
    val port: Int,
) {

    companion object {

        fun tryParse(
            input: String,
        ): ServerPort? = input
            .toIntOrNull()
            ?.takeIf { it > 1024 }
            ?.let(::ServerPort)

        const val maxLength: Int = 5

        val default: ServerPort = ServerPort(
            port = 26385,
        )
    }
}