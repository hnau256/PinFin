package hnau.common.compose.uikit.bubble

data class Bubble(
    val text: String,
    val duration: BubbleDuration = BubbleDuration.default,
)
