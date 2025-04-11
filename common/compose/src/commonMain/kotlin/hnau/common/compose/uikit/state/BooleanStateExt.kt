package hnau.common.compose.uikit.state

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import arrow.core.identity

@Composable
fun Boolean.BooleanStateContent(
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<Boolean>.() -> ContentTransform,
    falseContent: @Composable () -> Unit = {},
    trueContent: @Composable () -> Unit,
) {
    StateContent(
        modifier = modifier,
        label = "Boolean",
        contentKey = ::identity,
        transitionSpec = transitionSpec,
    ) { localValue ->
        when (localValue) {
            false -> falseContent()
            true -> trueContent()
        }
    }
}