package hnau.common.color

import hnau.common.kotlin.serialization.MappingKSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable(RGBABytes.Serializer::class)
@JvmInline
value class RGBABytes(
    val rgba: UInt,
) {

    constructor(
        r: UByte,
        g: UByte,
        b: UByte,
        a: UByte = UByte.MAX_VALUE,
    ) : this(
        rgba = r.toUInt().shl(24) +
                g.toUInt().shl(16) +
                b.toUInt().shl(8) +
                a.toUInt().shl(0),
    )

    val r: UByte
        get() = rgba.shr(24).toUByte()

    val g: UByte
        get() = rgba.shr(16).toUByte()

    val b: UByte
        get() = rgba.shr(8).toUByte()

    val a: UByte
        get() = rgba.shr(0).toUByte()

    override fun toString(): String = RGBABytes
        .getStringMapper(useAlpha = true)
        .reverse(this)


    object Serializer : MappingKSerializer<String, RGBABytes>(
        base = String.serializer(),
        mapper = getStringMapper(useAlpha = true),
    )

    companion object {

        fun rgba(
            rgba: UInt,
        ): RGBABytes = RGBABytes(
            rgba = rgba,
        )

        fun rgba(
            rgba: Long,
        ): RGBABytes = rgba(
            rgba = rgba.toUInt(),
        )

        fun rgb(
            rgb: Int,
        ): RGBABytes = rgba(
            rgba = rgb.shl(8).toLong() + 0xff
        )

        fun rgb(
            rgb: UInt,
        ): RGBABytes = rgb(
            rgb = rgb.toInt(),
        )

        val White: RGBABytes = rgb(0xffffff)
        val Black: RGBABytes = rgb(0x000000)

        val Red: RGBABytes = rgb(0xff0000)
        val Yellow: RGBABytes = rgb(0xffff00)
        val Green: RGBABytes = rgb(0x00ff00)
        val Cyan: RGBABytes = rgb(0x00ffff)
        val Blue: RGBABytes = rgb(0x0000ff)
        val Magenta: RGBABytes = rgb(0xff00ff)
    }
}