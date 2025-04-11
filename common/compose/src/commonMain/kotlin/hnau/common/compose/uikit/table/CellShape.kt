package hnau.common.compose.uikit.table

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import hnau.common.compose.uikit.utils.Dimens

class CellShape(
    private val cellScope: CellScope,
): Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val corners = cellScope.corners
        val radius: (opened: Boolean) -> CornerRadius = { opened ->
            CornerRadius(
                with(density) {
                    when (opened) {
                        true -> Dimens.cornerRadius.toPx()
                        false -> Dimens.cornerRadiusMin.toPx()
                    }
                }
            )
        }

        val leftTopRadius = radius(
            when (layoutDirection) {
                LayoutDirection.Ltr -> corners.startTopIsOpened
                LayoutDirection.Rtl -> corners.endTopIsOpened
            },
        )

        val rightTopRadius = radius(
            when (layoutDirection) {
                LayoutDirection.Ltr -> corners.endTopIsOpened
                LayoutDirection.Rtl -> corners.startTopIsOpened
            },
        )

        val leftBottomRadius = radius(
            when (layoutDirection) {
                LayoutDirection.Ltr -> corners.startBottomIsOpened
                LayoutDirection.Rtl -> corners.endBottomIsOpened
            },
        )

        val rightBottomRadius = radius(
            when (layoutDirection) {
                LayoutDirection.Ltr -> corners.endBottomIsOpened
                LayoutDirection.Rtl -> corners.startBottomIsOpened
            },
        )

        return Outline.Rounded(
            RoundRect(
                rect = size.toRect(),
                topLeft = leftTopRadius,
                topRight = rightTopRadius,
                bottomLeft = leftBottomRadius,
                bottomRight = rightBottomRadius,
            ),
        )
    }
}