package hnau.common.compose.uikit.table.layout

import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo

data class LayoutWeightElement(
    val weight: Float,
) : ModifierNodeElement<LayoutWeightNode>() {

    override fun create(): LayoutWeightNode = LayoutWeightNode(
        weight = weight,
    )

    override fun update(node: LayoutWeightNode) {
        node.weight = weight
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "weight"
        value = weight
        properties["weight"] = weight
    }
}