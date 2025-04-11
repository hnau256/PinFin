package hnau.common.compose.uikit.bubble

import kotlinx.coroutines.flow.StateFlow

interface BubblesProvider {

    val visibleBubble: StateFlow<Bubble?>
}
