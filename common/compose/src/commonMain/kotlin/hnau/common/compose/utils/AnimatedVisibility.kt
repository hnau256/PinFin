package hnau.common.compose.utils

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import arrow.core.Option
import arrow.core.Some
import arrow.core.identity
import arrow.core.toOption
import hnau.common.kotlin.Mutable

data class AnimatedVisibilityTransitions(
    val enter: EnterTransition,
    val exit: ExitTransition,
) {

    companion object {

        val static = AnimatedVisibilityTransitions(
            enter = fadeIn(),
            exit = fadeOut(),
        )

        val vertical = AnimatedVisibilityTransitions(
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        )

        val horizontal = AnimatedVisibilityTransitions(
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        )
    }
}


@Composable
fun <T, L> AnimatedVisibility(
    value: T,
    toLocal: T.() -> Option<L>,
    transitions: AnimatedVisibilityTransitions,
    modifier: Modifier = Modifier,
    label: String = "AnimatedVisibility",
    content: @Composable AnimatedVisibilityScope.(L) -> Unit,
) {
    val localOrNone = value.toLocal()
    androidx.compose.animation.AnimatedVisibility(
        visible = localOrNone.isDefined(),
        modifier = modifier,
        enter = transitions.enter,
        exit = transitions.exit,
        label = label,
    ) {
        var lastLocalOrNull by remember<Mutable<L?>> { Mutable(null) }
        if (localOrNone is Some<L>) {
            lastLocalOrNull = localOrNone.value
        }
        val lastLocal = lastLocalOrNull!!
        content(lastLocal)
    }
}

@Composable
fun <T> AnimatedNullableVisibility(
    value: T,
    transitions: AnimatedVisibilityTransitions,
    modifier: Modifier = Modifier,
    label: String = "AnimatedVisibility",
    content: @Composable AnimatedVisibilityScope.(T & Any) -> Unit,
): Unit = AnimatedVisibility(
    value = value,
    toLocal = { toOption() },
    transitions = transitions,
    modifier = modifier,
    label = label,
    content = content,
)

@Composable
fun <T> AnimatedOptionVisibility(
    value: Option<T>,
    transitions: AnimatedVisibilityTransitions,
    modifier: Modifier = Modifier,
    label: String = "AnimatedVisibility",
    content: @Composable AnimatedVisibilityScope.(T) -> Unit,
): Unit = AnimatedVisibility(
    value = value,
    toLocal = ::identity,
    transitions = transitions,
    modifier = modifier,
    label = label,
    content = content,
)
