package hnau.common.compose.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import hnau.common.color.material.MaterialColors
import hnau.common.color.material.MaterialHue
import hnau.common.color.material.get
import scheme.Scheme

actual fun buildPrettyColorScheme(
    primaryHue: MaterialHue,
    brightness: ThemeBrightness,
): ColorScheme {
    val argb = MaterialColors[primaryHue].v600.let { color ->
        color.r.toInt().shl(16) +
                color.g.toInt().shl(8) +
                color.b.toInt().shl(0)
    }
    val scheme = when (brightness) {
        ThemeBrightness.Dark -> Scheme.dark(argb)
        ThemeBrightness.Light -> Scheme.light(argb)
    }

    val primary = Color(scheme.primary)
    val onPrimary = Color(scheme.onPrimary)
    val secondary = Color(scheme.secondary)
    val onSecondary = Color(scheme.onSecondary)
    val tertiary = Color(scheme.tertiary)
    val onTertiary = Color(scheme.onTertiary)
    val primaryContainer = Color(scheme.primaryContainer)
    val onPrimaryContainer = Color(scheme.onPrimaryContainer)
    val secondaryContainer = Color(scheme.secondaryContainer)
    val onSecondaryContainer = Color(scheme.onSecondaryContainer)
    val tertiaryContainer = Color(scheme.tertiaryContainer)
    val onTertiaryContainer = Color(scheme.onTertiaryContainer)
    val surface = Color(scheme.surface)
    val onSurface = Color(scheme.onSurface)
    val surfaceVariant = Color(scheme.surfaceVariant)
    val onSurfaceVariant = Color(scheme.onSurfaceVariant)
    val surfaceContainerLowest = Color(scheme.surface)
    val surfaceContainerLow = Color(scheme.surface)
    val surfaceContainer = Color(scheme.surface)
    val surfaceContainerHigh = Color(scheme.surface)
    val surfaceContainerHighest = Color(scheme.surface)
    val surfaceBright = Color(scheme.surface)
    val surfaceDim = Color(scheme.surface)
    val surfaceTint = Color(scheme.surface)
    val inverseSurface = Color(scheme.inverseSurface)
    val inverseOnSurface = Color(scheme.inverseOnSurface)
    val inversePrimary = Color(scheme.inversePrimary)
    val background = Color(scheme.background)
    val onBackground = Color(scheme.onBackground)
    val error = Color(scheme.error)
    val onError = Color(scheme.onError)
    val outline = Color(scheme.outline)
    val outlineVariant = Color(scheme.outlineVariant)
    val scrim = Color(scheme.scrim)

    return when (brightness) {
        ThemeBrightness.Dark -> darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            tertiary = tertiary,
            onTertiary = onTertiary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceBright = surfaceBright,
            surfaceDim = surfaceDim,
            surfaceTint = surfaceTint,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            background = background,
            onBackground = onBackground,
            error = error,
            onError = onError,
            outline = outline,
            outlineVariant = outlineVariant,
            scrim = scrim,
        )

        ThemeBrightness.Light -> lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            tertiary = tertiary,
            onTertiary = onTertiary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceBright = surfaceBright,
            surfaceDim = surfaceDim,
            surfaceTint = surfaceTint,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            background = background,
            onBackground = onBackground,
            error = error,
            onError = onError,
            outline = outline,
            outlineVariant = outlineVariant,
            scrim = scrim,
        )
    }
}