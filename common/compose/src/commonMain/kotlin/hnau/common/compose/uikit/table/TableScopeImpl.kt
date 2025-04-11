package hnau.common.compose.uikit.table

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.table.layout.LayoutWeightElement

class TableScopeImpl(
    override val orientation: TableOrientation,
    private val parentCellScope: CellScope,
) : TableScope {

    private var previousCellScope: CellScopeImpl? = null

    override fun Modifier.weight(
        weight: Float,
    ): Modifier = then(
        LayoutWeightElement(
            weight = weight,
        )
    )

    @Composable
    override fun Cell(
        content: @Composable CellScope.() -> Unit,
    ) {
        val cellScope = CellScopeImpl(
            orientation = orientation,
            parentCellScope = parentCellScope,
        )
        previousCellScope?.let { last ->
            last.closeEndOrBottom()
            cellScope.closeStartOrTop()
        }
        cellScope.content()
        previousCellScope = cellScope
    }
}