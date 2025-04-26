package hnau.pinfin.upchain

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Update(
    val value: String,
)