package hnau.pinfin.projector.utils

import androidx.compose.material3.ColorScheme

fun ColorScheme.copyContainerToBase(): ColorScheme = copy(
    primary = primaryContainer,
    onPrimary = onPrimaryContainer,
    secondary = secondaryContainer,
    onSecondary = onSecondaryContainer,
    tertiary = tertiaryContainer,
    onTertiary = onTertiaryContainer,
    surface = surfaceContainer,
)