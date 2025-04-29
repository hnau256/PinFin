package hnau.pinfin.model.sync.utils

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class ServerAddress(
    val address: String,
) {

    companion object {

        fun tryParse(
            input: String,
        ): ServerAddress? = input
            .takeIf(String::isNotEmpty)
            ?.let(::ServerAddress)

        val empty: ServerAddress = ServerAddress(
            address = "",
        )
    }
}