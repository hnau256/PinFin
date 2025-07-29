package hnau.pinfin.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Icon(
    val key: String,
)