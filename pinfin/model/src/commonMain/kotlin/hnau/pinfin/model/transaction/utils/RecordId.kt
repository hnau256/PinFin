package hnau.pinfin.model.transaction.utils

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@JvmInline
value class RecordId(
    val id: String,
) {

    companion object {

        fun createNew(): RecordId = RecordId(
            id = UUID.randomUUID().toString(),
        )
    }
}