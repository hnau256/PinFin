package hnau.common.compose.utils

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon as PlatformIcon

@Composable
fun Icon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) = PlatformIcon(
    modifier = modifier,
    imageVector = icon,
    contentDescription = null,
    tint = tint,
)
