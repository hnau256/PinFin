package hnau.common.compose.uikit.table

class CellScopeImpl(
    private val parentCellScope: CellScope,
    private val orientation: TableOrientation,
) : CellScope {

    private var closeStartOrTop = false
    private var closeEndOrBottom = false

    override val corners: TableCorners
        get() = parentCellScope.corners.close(
            orientation = orientation,
            startOrTop = closeStartOrTop,
            endOrBottom = closeEndOrBottom,
        )

    fun closeStartOrTop() {
        closeStartOrTop = true
    }

    fun closeEndOrBottom() {
        closeEndOrBottom = true
    }
}