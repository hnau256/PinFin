package hnau.pinfin.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.math.absoluteValue

@Serializable
@JvmInline
value class Hue(
    val degrees: Int,
) {

    companion object {

        fun calcDefault(
            hash: Int,
        ): Hue = Hue(
            degrees = (hash % 360).absoluteValue,
        )
    }
}