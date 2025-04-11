package hnau.common.color

fun RGBABytes.copy(
    r: UByte = this.r,
    g: UByte = this.g,
    b: UByte = this.b,
    a: UByte = this.a,
): RGBABytes = RGBABytes(
    r = r,
    g = g,
    b = b,
    a = a,
)

operator fun RGBABytes.get(
    index: Int,
): UByte = when (index) {
    0 -> r
    1 -> g
    2 -> b
    3 -> a
    else -> throw IndexOutOfBoundsException(index)
}

operator fun RGBABytes.component1(): UByte = r

operator fun RGBABytes.component2(): UByte = g

operator fun RGBABytes.component3(): UByte = b

operator fun RGBABytes.component4(): UByte = a