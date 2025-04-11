package hnau.common.compose.uikit.table

enum class TableOrientation {
    Vertical, Horizontal,
    ;

    val opposite: TableOrientation
        get() = fold(
            ifVertical = { Horizontal },
            ifHorizontal = { Vertical },
        )

    inline fun <R> fold(
        ifVertical: () -> R,
        ifHorizontal: () -> R,
    ): R = when (this) {
        Vertical -> ifVertical()
        Horizontal -> ifHorizontal()
    }
}