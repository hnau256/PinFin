package hnau.pinfin.projector.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable

@Composable
fun Label(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = contentColorFor(containerColor),
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit,
) {
    val shape = MaterialTheme.shapes.small
    Box(
        modifier = modifier
            .then(
                selected.foldBoolean(
                    ifFalse = { Modifier },
                    ifTrue = {
                        Modifier.border(
                            shape = shape,
                            color = contentColor,
                            width = Dimens.border,
                        )
                    }
                )
            )
            .clip(shape)
            .then(
                onClick.foldNullable(
                    ifNull = { Modifier },
                    ifNotNull = {
                        Modifier.clickable(
                            onClick = it,
                        )
                    }
                )
            )
            .background(
                color = containerColor,
            )
            .padding(
                horizontal = Dimens.smallSeparation,
                vertical = Dimens.extraSmallSeparation,
            ),
        contentAlignment = contentAlignment,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalTextStyle provides MaterialTheme.typography.bodyMedium,
        ) {
            content()
        }
    }
}