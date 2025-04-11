package hnau.common.compose.uikit.state

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import arrow.core.Option

@Composable
fun <T> Option<T>.OptionStateContent(
    modifier: Modifier = Modifier,
    noneContent: @Composable () -> Unit = {},
    transitionSpec: AnimatedContentTransitionScope<Option<T>>.() -> ContentTransform,
    someContent: @Composable (value: T) -> Unit,
) {
    StateContent(
        modifier = modifier,
        label = "Option",
        contentKey = { localValue ->
            localValue.fold(
                ifEmpty = { false },
                ifSome = { true },
            )
        },
        transitionSpec = transitionSpec,
    ) { localValue ->
        localValue.fold(
            ifEmpty = { noneContent() },
            ifSome = { value -> someContent(value) }
        )
    }
}