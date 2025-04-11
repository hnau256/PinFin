package hnau.common.compose.uikit.table

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface TableScope {

    val orientation: TableOrientation

    fun Modifier.weight(
        weight: Float,
    ): Modifier

    @Composable
    fun Cell(
        content: @Composable CellScope.() -> Unit,
    )
}