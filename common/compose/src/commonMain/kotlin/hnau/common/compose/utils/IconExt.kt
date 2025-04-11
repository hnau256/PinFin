package hnau.common.compose.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon as PlatformIcon

@Composable
inline fun Icon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    chooseIcon: () -> ImageVector,
) = PlatformIcon(
    modifier = modifier,
    imageVector = chooseIcon(),
    contentDescription = null,
    tint = tint,
)
