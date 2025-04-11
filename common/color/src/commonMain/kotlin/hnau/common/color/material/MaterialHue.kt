package hnau.common.color.material

enum class MaterialHue(
    val hasAdditionalColors: Boolean = true,
) {
    Red,
    Pink,
    Purple,
    DeepPurple,
    Indigo,
    Blue,
    LightBlue,
    Cyan,
    Teal,
    Green,
    LightGreen,
    Lime,
    Yellow,
    Amber,
    Orange,
    DeepOrange,
    Brown(hasAdditionalColors = false),
    Grey(hasAdditionalColors = false),
    BlueGrey(hasAdditionalColors = false),
}