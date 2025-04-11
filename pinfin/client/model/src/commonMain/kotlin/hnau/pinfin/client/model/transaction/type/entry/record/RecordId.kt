package hnau.pinfin.client.model.transaction.type.entry.record

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@JvmInline
value class RecordId(
    val id: String,
) {

    companion object {

        fun new(): RecordId = UUID
            .randomUUID()
            .toString()
            .let(::RecordId)
    }
}