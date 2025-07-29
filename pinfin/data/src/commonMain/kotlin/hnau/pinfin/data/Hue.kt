package hnau.pinfin.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Hue(
    val value: Float,
) {

    companion object {

        fun calcDefault(
            hash: Int,
        ): Hue = Hue(
            value = (hash % 360) / 359f,
        )
    }
}