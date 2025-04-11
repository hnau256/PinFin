package hnau.common.compose.uikit.shape

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

@Composable
fun HnauShape.Companion.create(
    startTopRoundCorners: Boolean,
    endTopRoundCorners: Boolean,
    startBottomRoundCorners: Boolean,
    endBottomRoundCorners: Boolean,
): HnauShape {
    val startTopRadiusFraction by animateFloatAsState(
        targetValue = if (startTopRoundCorners) 1f else 0f,
    )
    val endTopRadiusFraction by animateFloatAsState(
        targetValue = if (endTopRoundCorners) 1f else 0f,
    )
    val startBottomRadiusFraction by animateFloatAsState(
        targetValue = if (startBottomRoundCorners) 1f else 0f,
    )
    val endBottomRadiusFraction by animateFloatAsState(
        targetValue = if (endBottomRoundCorners) 1f else 0f,
    )

    return HnauShape(
        startTopRadiusFraction = startTopRadiusFraction,
        endTopRadiusFraction = endTopRadiusFraction,
        startBottomRadiusFraction = startBottomRadiusFraction,
        endBottomRadiusFraction = endBottomRadiusFraction,
    )
}
