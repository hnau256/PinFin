package org.hnau.pinfin.projector.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.West
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import org.hnau.commons.gen.fold.annotations.Fold

object ArrowIcon {

    fun get(
        direction: ArrowDirection,
        layoutDirection: LayoutDirection,
    ): ImageVector = direction.fold(
        ifStartToEnd = {
            when (layoutDirection) {
                LayoutDirection.Ltr -> leftToRight
                LayoutDirection.Rtl -> rightToLeft
            }
        },
        ifEndToStart = {
            when (layoutDirection) {
                LayoutDirection.Ltr -> rightToLeft
                LayoutDirection.Rtl -> leftToRight
            }
        },
        ifBoth = { both },
    )

    @Composable
    operator fun get(
        direction: ArrowDirection,
    ): ImageVector = get(
        direction = direction,
        layoutDirection = LocalLayoutDirection.current,
    )

    private val leftToRight: ImageVector
        get() = Icons.Filled.East

    private val rightToLeft: ImageVector
        get() = Icons.Filled.West

    private val both: ImageVector
        get() = Icons.Filled.SyncAlt

}

@Fold
enum class ArrowDirection { StartToEnd, EndToStart, Both }