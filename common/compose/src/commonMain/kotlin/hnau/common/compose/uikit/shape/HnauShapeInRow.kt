package hnau.common.compose.uikit.shape

import androidx.compose.runtime.Composable

object HnauShapeInRow

val HnauShape.Companion.inRow get() = HnauShapeInRow

@Composable
fun HnauShapeInRow.create(
    startRoundCorners: Boolean,
    endRoundCorners: Boolean,
) = HnauShape.create(
    startTopRoundCorners = startRoundCorners,
    endTopRoundCorners = endRoundCorners,
    startBottomRoundCorners = startRoundCorners,
    endBottomRoundCorners = endRoundCorners,
)

val HnauShapeInRow.start
    @Composable
    get() = create(
        startRoundCorners = true,
        endRoundCorners = false,
    )

val HnauShapeInRow.center
    @Composable
    get() = create(
        startRoundCorners = false,
        endRoundCorners = false,
    )

val HnauShapeInRow.end
    @Composable
    get() = create(
        startRoundCorners = false,
        endRoundCorners = true,
    )

@Composable
fun HnauShapeInRow.create(
    index: Int,
    totalCount: Int,
): HnauShape = HnauShape.inRow.create(
    startRoundCorners = index == 0,
    endRoundCorners = index == totalCount - 1,
)
