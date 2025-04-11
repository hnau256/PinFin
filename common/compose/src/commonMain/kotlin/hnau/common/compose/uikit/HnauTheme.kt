package hnau.common.compose.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import arrow.core.Either
import hnau.common.color.material.MaterialHue
import hnau.common.compose.utils.DynamicColorsGenerator
import hnau.common.compose.utils.ThemeBrightness
import hnau.common.compose.utils.buildPrettyColorScheme

@Composable
fun HnauTheme(
    primaryHueOrDynamicColorsGenerator: Either<MaterialHue, DynamicColorsGenerator>,
    brightness: ThemeBrightness = ThemeBrightness.system,
    content: @Composable () -> Unit,
) = MaterialTheme(
    colorScheme = when (primaryHueOrDynamicColorsGenerator) {
        is Either.Left -> buildPrettyColorScheme(
            primaryHue = primaryHueOrDynamicColorsGenerator.value,
            brightness = brightness,
        )

        is Either.Right -> primaryHueOrDynamicColorsGenerator.value.generateDynamicColors(
            brightness = brightness,
        )
    },
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
        ) {
            content()
        }
    }
}