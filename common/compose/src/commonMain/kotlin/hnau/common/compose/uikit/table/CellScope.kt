package hnau.common.compose.uikit.table

interface CellScope {

    val corners: TableCorners

    companion object {

        val opened: CellScope = object : CellScope {

            override val corners: TableCorners
                get() = TableCorners.opened
        }
    }
}