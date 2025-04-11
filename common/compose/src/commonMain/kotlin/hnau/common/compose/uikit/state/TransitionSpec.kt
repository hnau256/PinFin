package hnau.common.compose.uikit.state

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntSize

object TransitionSpec {

    fun <S> crossfade(): AnimatedContentTransitionScope<S>.() -> ContentTransform =
        { enterFadeIn togetherWith exitFadeOut }

    fun <S> both(): AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        val enter = enterFadeIn + scaleIn(
            initialScale = 0.92f,
            animationSpec = enterFloatAnimationSpec
        )
        val exit = exitFadeOut + scaleOut(
            animationSpec = exitFloatAnimationSpec,
            targetScale = 0.8f,
        )
        enter togetherWith exit
    }

    fun <S> horizontal(): AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        val enter = enterFadeIn + expandHorizontally(
            animationSpec = enterIntSizeAnimationSpec,
            expandFrom = Alignment.CenterHorizontally,
        )
        val exit = exitFadeOut + shrinkHorizontally(
            animationSpec = exitIntSizeAnimationSpec,
            shrinkTowards = Alignment.CenterHorizontally,
        )
        enter togetherWith exit
    }

    fun <S> vertical(): AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        val enter = enterFadeIn + expandVertically(
            animationSpec = enterIntSizeAnimationSpec,
            expandFrom = Alignment.CenterVertically,
        )
        val exit = exitFadeOut + shrinkVertically(
            animationSpec = exitIntSizeAnimationSpec,
            shrinkTowards = Alignment.CenterVertically,
        )
        enter togetherWith exit
    }

    private const val EnterDurationMillis = 220
    private const val ExitDurationMillis = 90

    private val enterFloatAnimationSpec = tween<Float>(
        durationMillis = EnterDurationMillis,
    )

    private val enterIntSizeAnimationSpec = tween<IntSize>(
        durationMillis = EnterDurationMillis,
    )

    private val enterFadeIn = fadeIn(
        animationSpec = enterFloatAnimationSpec,
    )

    private val exitFloatAnimationSpec = tween<Float>(
        durationMillis = ExitDurationMillis
    )

    private val exitIntSizeAnimationSpec = tween<IntSize>(
        durationMillis = ExitDurationMillis
    )

    private val exitFadeOut = fadeOut(
        animationSpec = exitFloatAnimationSpec,
    )
}