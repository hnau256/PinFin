package hnau.common.compose.uikit.table

import androidx.compose.ui.graphics.Shape


val CellScope.cellShape: Shape
    get() = CellShape(
        cellScope = this,
    )