package hnau.common.compose.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
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

fun Modifier.horizontalDisplayPadding(): Modifier = padding(
    horizontal = Dimens.separation,
)

fun Modifier.verticalDisplayPadding(): Modifier = padding(
    vertical = Dimens.separation,
)