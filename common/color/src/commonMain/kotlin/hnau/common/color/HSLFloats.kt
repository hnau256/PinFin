package hnau.common.color

import hnau.common.kotlin.mapper.Mapper
import kotlinx.serialization.Serializable

@Serializable
data class HSLFloats(
    val h: Float,
    val s: Float,
    val l: Float,
) {

    companion object {

        val rgbFloatsMapper: Mapper<RGBFloats, HSLFloats> = Mapper(
            direct = { (r, g, b) ->
                var max = r
                var min = r
                var maxChannel = RGBChannel.R
                listOf(
                    RGBChannel.G to g,
                    RGBChannel.B to b,
                ).forEach { (channel, value) ->
                    if (value > max) {
                        max = value
                        maxChannel = channel
                    } else {
                        if (value < min) {
                            min = value
                        }
                    }
                }
                val delta = max - min
                val sum = min + max
                HSLFloats(
                    h = when {
                        delta > 0 -> when (maxChannel) {
                            RGBChannel.R -> when (g >= b) {
                                true -> (g - b) / delta
                                false -> (g - b) / delta + 6
                            }

                            RGBChannel.G -> (b - r) / delta + 2
                            RGBChannel.B -> (r - g) / delta + 4
                        }

                        else -> 0f
                    } / 6,
                    s = when {
                        sum <= 0 || max == min -> 0f
                        sum < 1 -> delta / sum
                        else -> delta / (2 - sum)
                    },
                    l = sum / 2,
                )
            },
            reverse = { (h, s, l) ->
                val q = when {
                    l < 0.5 -> l * (1 + s)
                    else -> l + s - (l * s)
                }
                val p = 2 * l - q
                listOf(
                    h + 1f / 3f,
                    h,
                    h - 1f / 3f,
                )
                    .map { value ->
                        val normalizedValue = when {
                            value < 0 -> value + 1
                            value > 1 -> value - 1
                            else -> value
                        }
                        when {
                            normalizedValue < 1f / 6f -> p + ((q - p) * 6 * normalizedValue)
                            normalizedValue < 1f / 2f -> q
                            normalizedValue < 2f / 3f -> p + ((q - p) * 6 * (2f / 3f - normalizedValue))
                            else -> p
                        }
                    }
                    .let { (r, g, b) ->
                        RGBFloats(r, g, b)
                    }
            }
        )
    }
}