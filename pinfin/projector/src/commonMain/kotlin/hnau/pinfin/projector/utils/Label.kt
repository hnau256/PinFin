package hnau.pinfin.projector.utils

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import hnau.common.app.projector.uikit.utils.Dimens

@Composable
fun Label(
    modifier: Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                color = containerColor,
                shape = MaterialTheme.shapes.small,
            )
            .padding(
                horizontal = Dimens.smallSeparation,
                vertical = Dimens.extraSmallSeparation,
            ),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalTextStyle provides MaterialTheme.typography.bodyMedium,
        ) {
            content()
        }
    }
}