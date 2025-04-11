package hnau.common.compose.uikit.state

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> T?.NullableStateContent(
    modifier: Modifier = Modifier,
    label: String = "ValueOrNull",
    nullContent: @Composable () -> Unit = {},
    transitionSpec: AnimatedContentTransitionScope<T?>.() -> ContentTransform,
    anyContent: @Composable (value: T & Any) -> Unit,
) {
    StateContent(
        modifier = modifier,
        label = label,
        contentKey = { localValue -> localValue != null },
        transitionSpec = transitionSpec,
    ) { localValue ->
        when (localValue) {
            null -> nullContent()
            else -> anyContent(localValue)
        }
    }
}