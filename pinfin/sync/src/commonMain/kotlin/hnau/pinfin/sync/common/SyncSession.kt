package hnau.pinfin.model.sync.utils

import hnau.common.kotlin.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@JvmInline
value class SyncSession(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
)