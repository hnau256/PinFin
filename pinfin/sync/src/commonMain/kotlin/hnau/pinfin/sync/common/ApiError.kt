package hnau.pinfin.model.sync.utils

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ApiError(
    val message: String?,
)