package hnau.pinfin.projector.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import hnau.common.app.projector.uikit.shape.HnauShape
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon

@Composable
fun NotSelectedButton(
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    shape: Shape = HnauShape(),
    content: @Composable () -> Unit,
) {
    Button(
        modifier = modifier,
        shape = shape,
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        colors = ButtonDefaults.colors(
            container = MaterialTheme.colorScheme.error,
        )
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyMedium,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.extraSmallSeparation),
            ) {
                Icon(Icons.AutoMirrored.Filled.Help)
                content()
            }
        }
    }
}