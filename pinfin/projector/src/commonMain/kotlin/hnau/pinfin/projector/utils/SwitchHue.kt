package hnau.pinfin.projector.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dynamiccolor.Variant
import hnau.common.app.model.theme.ThemeBrightness
import hnau.common.app.model.theme.ThemeBrightnessValues
import hnau.common.app.projector.utils.theme.DynamicSchemeConfig
import hnau.common.app.projector.utils.theme.buildColorScheme
import hnau.common.app.projector.utils.theme.themeBrightness
import hnau.pinfin.data.Hue
import hnau.pinfin.model.utils.model

private val DynamicSchemeConfigForHue = DynamicSchemeConfig(
    variant = Variant.FIDELITY,
    contrastLevel = 0.0,
    //chroma = 100.0,
    /*tone = ThemeBrightnessValues(
        light = 50.0,
        dark = 50.0,
    )*/
)

private val schemesCache: MutableMap<Hue, MutableMap<ThemeBrightness, ColorScheme>> = HashMap()

@Composable
fun SwitchHue(
    hue: Hue,
    content: @Composable () -> Unit,
) {
    val brightness = MaterialTheme.themeBrightness
    val scheme = schemesCache
        .getOrPut(hue) { HashMap() }
        .getOrPut(brightness) {
            buildColorScheme(
                hue = hue.model,
                config = DynamicSchemeConfigForHue,
                brightness = brightness,
            )
        }
    MaterialTheme(
        colorScheme = scheme,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.primaryContainer,
        ) {
            content()
        }
    }
}