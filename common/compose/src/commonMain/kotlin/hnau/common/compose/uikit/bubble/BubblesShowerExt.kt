package hnau.common.compose.uikit.bubble

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun BubblesShower.showBubbles(
    scope: CoroutineScope,
    bubbles: Flow<Bubble>,
) {
    scope.launch {
        bubbles.collect(::showBubble)
    }
}
