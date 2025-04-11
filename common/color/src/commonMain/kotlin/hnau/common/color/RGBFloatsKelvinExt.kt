package hnau.common.color

import hnau.common.color.gradient.Gradient
import hnau.common.color.gradient.create
import kotlin.math.ln
import kotlin.math.pow

fun RGBFloats.Companion.fromKelvin(
    kelvin: Float,
): RGBFloats {
    val normalized: Double = kelvin.coerceIn(1000f, 40000f) / 100.0
    val (r, g, b) = if (normalized < 66) {
        RGBFloats(
            r = 255f,
            g = (99.4708025861 * ln(normalized) - 161.1195681661).toFloat(),
            b = if (normalized < 19) {
                0f
            } else {
                (138.5177312231 * ln(normalized - 10.0) - 305.0447927307).toFloat()
            }
        )
    } else {
        RGBFloats(
            r = (329.698727446 * normalized.pow(-0.1332047592)).toFloat(),
            g = (288.1221695283 * normalized.pow(-0.0755148492)).toFloat(),
            b = 255f,
        )
    }
    return RGBFloats(
        r = r.coerceIn(0f, 1f),
        g = g.coerceIn(0f, 1f),
        b = b.coerceIn(0f, 1f),
    )
}

fun RGBFloats.Companion.kelvinGradient(
    from: Float,
    to: Float,
    stops: Int = 32,
): Gradient<RGBFloats> {
    val delta = to - from
    return Gradient.create(
        stops = stops,
    ) { fraction ->
        RGBFloats.fromKelvin(from + fraction * delta)
    }
}