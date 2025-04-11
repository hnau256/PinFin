package hnau.common.compose.projector.stack

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.compose.utils.getTransitionSpecForHorizontalSlide
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.seconds

@Composable
fun <K, P> StateFlow<StackProjectorTail<K, P>>.Content(
    content: @Composable (projector: P) -> Unit,
) {
    val currentTail: StackProjectorTail<K, P> by collectAsState()
    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = currentTail,
        contentKey = { tail -> tail.key },
        transitionSpec = getTransitionSpecForHorizontalSlide(
            duration = AnimationDuration,
        ) {
            when (targetState.isNew) {
                true -> AnimationSlideFactor
                false -> -AnimationSlideFactor
                null -> 0
            }
        },
        contentAlignment = Alignment.Center,
    ) { (projector, _) ->
        content(projector)
    }
}

private const val AnimationSlideFactor = 0.5
private val AnimationDuration = 0.3.seconds
