package hnau.pinfin.projector.transaction.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import hnau.pinfin.projector.utils.SlideOrientation
import hnau.pinfin.projector.utils.getTransitionSpecForSlide
import kotlin.math.sign

inline fun <T> createPagesTransitionSpec(
    orientation: SlideOrientation,
    crossinline extractIndex: (T) -> Int,
): AnimatedContentTransitionScope<T>.() -> ContentTransform = getTransitionSpecForSlide(
    orientation = orientation,
) {
    (extractIndex(targetState) - extractIndex(initialState)).sign * 0.5
}