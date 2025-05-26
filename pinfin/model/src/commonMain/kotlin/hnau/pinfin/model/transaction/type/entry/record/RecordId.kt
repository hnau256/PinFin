package hnau.pinfin.model.transaction.type.entry.record

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class RecordId(
    val id: String,
) {

    companion object {

        fun new(): RecordId = Uuid
            .random()
            .toString()
            .let(::RecordId)
    }
}