package hnau.pinfin.sync.common

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ApiError(
    val message: String?,
)