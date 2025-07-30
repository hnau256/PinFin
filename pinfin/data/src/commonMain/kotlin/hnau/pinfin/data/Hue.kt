package hnau.pinfin.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Hue(
    val degrees: Int,
) {

    companion object {

        fun calcDefault(
            hash: Int,
        ): Hue = Hue(
            degrees = hash % 360,
        )
    }
}