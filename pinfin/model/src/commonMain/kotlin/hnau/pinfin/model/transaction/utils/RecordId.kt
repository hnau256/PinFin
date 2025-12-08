package hnau.pinfin.model.transaction.utils

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class RecordId(
    val id: String,
) {

    companion object {

        @OptIn(ExperimentalUuidApi::class)
        fun createNew(): RecordId = RecordId(
            id = Uuid.random().toString(),
        )
    }
}