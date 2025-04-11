package hnau.common.color

import hnau.common.kotlin.ifNull
import hnau.common.kotlin.ifTrue
import hnau.common.kotlin.map
import hnau.common.kotlin.mapper.Mapper

private val stringToRGBABytesMapper: Mapper<String, RGBABytes> =
    createStringToRGBABytesMapper(useAlpha = true)

private val stringToRGBABytesMapperWithoutAlpha: Mapper<String, RGBABytes> =
    createStringToRGBABytesMapper(useAlpha = false)

fun RGBABytes.Companion.getStringMapper(
    useAlpha: Boolean,
): Mapper<String, RGBABytes> = when (useAlpha) {
    true -> stringToRGBABytesMapper
    false -> stringToRGBABytesMapperWithoutAlpha
}

private fun createStringToRGBABytesMapper(
    useAlpha: Boolean,
): Mapper<String, RGBABytes> = Mapper(
    direct = { string ->
        string
            .takeIf { it.firstOrNull() == Prefix }
            .ifNull {
                throwFormatException(
                    useAlpha = useAlpha,
                    string = string,
                    reason = "no `$Prefix` prefix",
                )
            }
            .drop(1)
            .uppercase()
            .map { char ->
                charHalfByteMapper
                    .direct(char)
                    .also { byte ->
                        val int = byte.toInt()
                        if (int < 0 || int > 15) {
                            throwFormatException(
                                useAlpha = useAlpha,
                                string = string,
                                reason = "illegal symbol `$char` (code=$int)",
                            )
                        }
                    }
            }
            .let { bytes ->
                val bytesCount = bytes.size
                when {
                    bytesCount == 3 -> {
                        val (r, g, b) = bytes
                        RGBABytes(
                            r = r.duplicate(),
                            g = g.duplicate(),
                            b = b.duplicate(),
                        )
                    }

                    useAlpha && bytesCount == 4 -> {
                        val (r, g, b, a) = bytes
                        RGBABytes(
                            r = r.duplicate(),
                            g = g.duplicate(),
                            b = b.duplicate(),
                            a = a.duplicate(),
                        )
                    }

                    bytesCount == 6 -> {
                        RGBABytes(
                            r = joinHalfBytes(bytes[0], bytes[1]),
                            g = joinHalfBytes(bytes[2], bytes[3]),
                            b = joinHalfBytes(bytes[4], bytes[5]),
                        )
                    }

                    useAlpha && bytesCount == 8 -> {
                        RGBABytes(
                            r = joinHalfBytes(bytes[0], bytes[1]),
                            g = joinHalfBytes(bytes[2], bytes[3]),
                            b = joinHalfBytes(bytes[4], bytes[5]),
                            a = joinHalfBytes(bytes[6], bytes[7]),
                        )
                    }

                    else -> throwFormatException(
                        useAlpha = useAlpha,
                        string = string,
                        reason = "incorrect length",
                    )
                }
            }
    },
    reverse = { rgbaBytes ->
        listOfNotNull(
            rgbaBytes.r,
            rgbaBytes.g,
            rgbaBytes.b,
            rgbaBytes.a.takeIf { useAlpha && it != UByte.MAX_VALUE },
        )
            .map { channel ->
                channel
                    .toUInt()
                    .let { int ->
                        Pair(
                            first = int.shr(4),
                            second = int,
                        ).map { half ->
                            half
                                .and(0xf.toUInt())
                                .toUByte()
                        }
                    }
            }
            .let { channels ->
                when (channels.all { (a, b) -> a == b }) {
                    true -> channels.map { channel ->
                        channel.first
                    }

                    false -> channels.flatMap { channel ->
                        listOf(channel.first, channel.second)
                    }
                }
            }
            .joinToString(
                separator = "",
                prefix = Prefix.toString(),
            ) { byte ->
                charHalfByteMapper.reverse(byte).toString()
            }
    }
)

private const val Prefix = '#'

private fun UByte.duplicate(): UByte =
    joinHalfBytes(this, this)

private fun joinHalfBytes(halfByte1: UByte, halfByte2: UByte): UByte =
    (halfByte1 * 16.toUByte() + halfByte2).toUByte()

private val charHalfByteMapper: Mapper<Char, UByte> = Mapper(
    direct = { char ->
        val code = char.code
        (code - '0'.code)
            .takeIf { it < 10 }
            .ifNull { code - 'A'.code + 10 }
            .toUByte()
    },
    reverse = { byte ->
        val int = byte.toInt()
        when {
            int < 10 -> Char('0'.code + int)
            else -> Char('A'.code + (int - 10))
        }
    }
)

private fun UByte.splitToHalfBytes(): Pair<UByte, UByte> = toUInt().let { int ->
    Pair(
        first = int.shr(4),
        second = int,
    ).map { half ->
        half
            .and(0xf.toUInt())
            .toUByte()
    }
}

private fun throwFormatException(
    useAlpha: Boolean,
    string: String,
    reason: String,
): Nothing = listOf('R', 'G', 'B')
    .let { channels ->
        listOfNotNull(
            channels,
            useAlpha.ifTrue { channels + 'A' },
        )
    }
    .flatMap { channels ->
        listOf(
            channels,
            channels.flatMap { listOf(it, it) }
        )
    }
    .joinToString(
        separator = " or ",
    ) { signs ->
        signs.joinToString(
            separator = "",
            prefix = Prefix.toString(),
            transform = Char::toString,
        )
    }
    .let { formats ->
        throw IllegalArgumentException("Unable parse `$string` to RGBABytes: $reason. Expected $formats format")
    }