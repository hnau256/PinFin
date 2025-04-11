package hnau.common.color

import hnau.common.kotlin.mapper.Mapper
import kotlinx.serialization.Serializable

@Serializable
data class RGBFloats(
    val r: Float,
    val g: Float,
    val b: Float,
) {

    operator fun get(
        channel: RGBChannel,
    ): Float = when (channel) {
        RGBChannel.R -> r
        RGBChannel.G -> g
        RGBChannel.B -> b
    }

    companion object {

        val rgbaBytesMapper: Mapper<RGBABytes, RGBFloats> = run {
            val channelMapper: Mapper<UByte, Float> = Mapper(
                direct = { byte -> byte.toFloat() / 255f },
                reverse = { float -> (float * 255).toInt().toUByte() }
            )
            Mapper(
                direct = { (r, g, b) ->
                    RGBFloats(
                        r = channelMapper.direct(r),
                        g = channelMapper.direct(g),
                        b = channelMapper.direct(b),
                    )
                },
                reverse = { (r, g, b) ->
                    RGBABytes(
                        r = channelMapper.reverse(r),
                        g = channelMapper.reverse(g),
                        b = channelMapper.reverse(b),
                    )
                }
            )
        }
    }
}