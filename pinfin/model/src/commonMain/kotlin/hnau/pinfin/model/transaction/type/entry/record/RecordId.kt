package hnau.pinfin.model.transaction.type.entry.record

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class RecordId(
    val id: String,
) {

    companion object {

        @OptIn(ExperimentalUuidApi::class)
        fun new(): RecordId = Uuid
            .random()
            .toString()
            .let(::RecordId)
    }
}