package hnau.common.compose.uikit.bubble

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import hnau.common.compose.uikit.utils.Dimens

@Composable
fun BubblesProvider.Content(
    modifier: Modifier = Modifier,
) {
    val bubble by visibleBubble.collectAsState()
    AnimatedContent(
        modifier = modifier
            .fillMaxSize(),
        targetState = bubble,
        transitionSpec = {
            val enterDelayMillis = 100
            val enterDurationMillis = 250
            val enterFade = fadeIn(
                animationSpec = tween(
                    durationMillis = enterDurationMillis,
                    delayMillis = enterDelayMillis,
                ),
            )
            val enterScale = scaleIn(
                initialScale = 0.9f,
                animationSpec = tween(
                    durationMillis = enterDurationMillis,
                    delayMillis = enterDelayMillis,
                ),
                transformOrigin = TransformOrigin(0.5f, 0f),
            )
            val enter = enterFade + enterScale

            val exitDurationMillis = 250
            val exitFade = fadeOut(
                animationSpec = tween(
                    durationMillis = exitDurationMillis,
                ),
            )
            val exitScale = scaleOut(
                targetScale = 0.9f,
                animationSpec = tween(
                    durationMillis = exitDurationMillis,
                ),
                transformOrigin = TransformOrigin(0.5f, 0f),
            )
            val exit = exitFade + exitScale

            enter togetherWith exit
        },
    ) { localBubbleOrNull ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(Dimens.separation),
            contentAlignment = Alignment.TopCenter,
        ) {
            localBubbleOrNull?.Content()
        }
    }
}

@Composable
private fun Bubble.Content(): Nothing = TODO()
