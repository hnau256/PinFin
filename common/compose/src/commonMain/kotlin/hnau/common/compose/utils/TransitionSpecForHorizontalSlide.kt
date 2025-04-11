package hnau.common.compose.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import kotlin.math.absoluteValue
import kotlin.math.sign
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun <T> getTransitionSpecForHorizontalSlide(
    duration: Duration = AnimationDuration,
    slideCoefficientProvider: AnimatedContentTransitionScope<T>.() -> Number,
): AnimatedContentTransitionScope<T>.() -> ContentTransform {
    val durationMillis = duration.inWholeMilliseconds.toInt()
    return {
        val slideCoefficient = slideCoefficientProvider().toFloat()
        val fade = slideCoefficient.absoluteValue < 1
        var enterTransition = slideInHorizontally(
            animationSpec = tween(durationMillis),
            initialOffsetX = { fullWidth -> (slideCoefficient * fullWidth).toInt() },
        )
        if (fade) {
            enterTransition += fadeIn(
                animationSpec = tween(durationMillis)
            )
        }
        var exitTransition = slideOutHorizontally(
            animationSpec = tween(durationMillis),
            targetOffsetX = { fullWidth -> (slideCoefficient * -fullWidth).toInt() },
        )
        if (fade) {
            exitTransition += fadeOut(
                animationSpec = tween(durationMillis)
            )
        }
        val result = enterTransition togetherWith exitTransition
        result.targetContentZIndex = slideCoefficient.sign
        result
    }
}

private val AnimationDuration = 0.5.seconds
