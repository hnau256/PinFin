package hnau.pinfin.model.sync.utils

import hnau.common.kotlin.mapper.Mapper
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

sealed interface ApiResponse<out T> {

    @Serializable
    @SerialName("success")
    data class Success<out T>(
        val data: T,
    ) : ApiResponse<T>

    @Serializable
    @SerialName("error")
    data class Error(
        val error: String?,
    ) : ApiResponse<Nothing>

    companion object {

        @OptIn(ExperimentalSerializationApi::class)
        fun <T> createByteArrayMapper(
            dataSerializer: KSerializer<T>,
        ): Mapper<ByteArray, ApiResponse<T>> = Mapper(
            direct = {
                val head = it.first()
                val tail = it.drop(1).toByteArray()
                when (head) {
                    zeroByte -> SyncConstants.cbor
                        .decodeFromByteArray(
                            deserializer = String.serializer(),
                            bytes = tail,
                        ).takeIf { it.isNotEmpty() }
                        .let(::Error)

                    else -> SyncConstants.cbor
                        .decodeFromByteArray(
                            deserializer = dataSerializer,
                            bytes = tail,
                        )
                        .let(::Success)
                }
            },
            reverse = { response ->
                val (head, tail) = when (response) {
                    is Success -> oneByte to SyncConstants.cbor
                        .encodeToByteArray(
                            serializer = dataSerializer,
                            value = response.data,
                        )

                    is Error -> zeroByte to SyncConstants.cbor
                        .encodeToByteArray(
                            serializer = String.serializer(),
                            value = response.error.orEmpty(),
                        )
                }
                ByteArray(tail.size + 1) { i ->
                    when (i) {
                        0 -> head
                        else -> tail[i - 1]
                    }
                }
            }
        )

        private const val zeroByte: Byte = 0
        private const val oneByte: Byte = 1
    }
}