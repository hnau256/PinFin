package hnau.common.compose.uikit.table.layout

import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.unit.Density
import hnau.common.kotlin.castOrNull
import hnau.common.kotlin.ifNull

data class LayoutWeightNode(
    var weight: Float,
) : ParentDataModifierNode, Modifier.Node() {

    override fun Density.modifyParentData(
        parentData: Any?,
    ): TableParentData = parentData
        .castOrNull<TableParentData>()
        .ifNull { TableParentData() }
        .also { tableParentData -> tableParentData.weight = weight }
}