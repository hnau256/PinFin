package hnau.common.compose.uikit.state

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun <T> T.StateContent(
    modifier: Modifier = Modifier,
    label: String,
    contentKey: (targetState: T) -> Any?,
    transitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform,
    content: @Composable AnimatedContentScope.(targetState: T) -> Unit,
) {
    AnimatedContent(
        targetState = this,
        contentKey = contentKey,
        modifier = modifier,
        contentAlignment = Alignment.Center,
        label = label,
        content = content,
        transitionSpec = transitionSpec,
    )
}