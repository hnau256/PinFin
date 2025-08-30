package hnau.pinfin.projector.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.absoluteValue
import kotlin.math.sign
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

//TODO extract to common-app

enum class SlideOrientation { Horizontal, Vertical }

fun <T> getTransitionSpecForSlide(
    duration: Duration = AnimationDuration,
    orientation: SlideOrientation,
    slideCoefficientProvider: AnimatedContentTransitionScope<T>.() -> Number,
): AnimatedContentTransitionScope<T>.() -> ContentTransform {
    val durationMillis = duration.inWholeMilliseconds.toInt()
    val animationSpecInt = tween<IntOffset>(durationMillis)
    val animationSpecFloat = tween<Float>(durationMillis)
    return {

        val slideCoefficient = slideCoefficientProvider().toFloat()
        val fade = slideCoefficient.absoluteValue < 1
        var enterTransition = slideIn(
            initialOffset = { it.toOffset(orientation) * slideCoefficient },
            animationSpec = animationSpecInt
        )
        if (fade) {
            enterTransition += fadeIn(
                animationSpec = animationSpecFloat
            )
        }
        var exitTransition = slideOut(
            targetOffset = { it.toOffset(orientation) * (-slideCoefficient) },
            animationSpec = animationSpecInt
        )
        if (fade) {
            exitTransition += fadeOut(
                animationSpec = animationSpecFloat
            )
        }
        val result = enterTransition togetherWith exitTransition
        result.targetContentZIndex = slideCoefficient.sign
        result
    }
}

private fun IntSize.toOffset(
    orientation: SlideOrientation,
): IntOffset = when (orientation) {
    SlideOrientation.Horizontal -> IntOffset(width, 0)
    SlideOrientation.Vertical -> IntOffset(0, height)
}

private val AnimationDuration = 0.3.seconds