package hnau.common.compose.uikit.backbutton

import androidx.compose.ui.unit.Dp

@JvmInline
value class BackButtonWidthFraction(
    val fraction: Float,
) {

    val width: Dp
        get() = BackButtonConstants.maxWidth * fraction
}