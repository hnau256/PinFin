package hnau.common.compose.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import hnau.common.compose.uikit.utils.Dimens


@Composable
fun Modifier.option(
    buildModifierOrNull: @Composable Modifier.() -> Modifier?,
): Modifier = buildModifierOrNull()
    ?.let(Modifier::then)
    ?: this

fun Modifier.clickableOption(
    onClick: (() -> Unit)?,
    onClickLabel: String? = null,
    role: Role? = null,
): Modifier = when (onClick) {
    null -> this
    else -> clickable(
        onClickLabel = onClickLabel,
        role = role,
        onClick = onClick,
    )
}

val Dimens.horizontalDisplayPadding: Dp
    get() = separation

fun Modifier.horizontalDisplayPadding(): Modifier = padding(
    horizontal = Dimens.horizontalDisplayPadding,
)

val Dimens.verticalDisplayPadding: Dp
    get() = separation

fun Modifier.verticalDisplayPadding(): Modifier = padding(
    vertical = Dimens.verticalDisplayPadding,
)