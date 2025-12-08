package hnau.pinfin.model.utils.budget.upchain

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class Update(
    val value: String,
)