package hnau.pinfin.client.compose.utils

import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color
import hnau.common.color.material.MaterialColors
import hnau.common.color.material.MaterialHue
import hnau.common.color.material.MaterialLightness
import hnau.common.color.material.get
import hnau.common.color.material.red
import hnau.common.compose.uikit.color.compose

fun buildColors(
    primaryHue: MaterialHue,
    secondaryHue: MaterialHue,
    isDark: Boolean,
): Colors {
    val config: ColorsConfig = when (isDark) {
        true -> ColorsConfig.dark
        false -> ColorsConfig.light
    }
    return Colors(
        isLight = !isDark,
        primary = MaterialColors[primaryHue][config.lightness].compose,
        primaryVariant = MaterialColors[primaryHue][config.variantLightness].compose,
        onPrimary = config.foreground,
        secondary = MaterialColors[secondaryHue][config.lightness].compose,
        secondaryVariant = MaterialColors[secondaryHue][config.variantLightness].compose,
        onSecondary = config.foreground,
        surface = config.surface,
        onSurface = config.foreground,
        background = config.background,
        onBackground = config.foreground,
        error = MaterialColors.red[config.errorLightness].compose,
        onError = config.foreground,
    )
}

private data class ColorsConfig(
    val background: Color,
    val surface: Color,
    val foreground: Color,
    val lightness: MaterialLightness,
    val variantLightness: MaterialLightness,
    val errorLightness: MaterialLightness,
) {

    companion object {
        private val darkestGrey = Color(0xff090909)
        private val darkGrey = Color(0xff101010)
        private val lightGrey = Color(0xfff0f0f0)
        private val lightestGrey = Color(0xfff7f7f7)

        val light = ColorsConfig(
            background = lightestGrey,
            surface = lightGrey,
            foreground = darkestGrey,
            lightness = MaterialLightness.V600,
            variantLightness = MaterialLightness.V800,
            errorLightness = MaterialLightness.V600,
        )

        val dark = ColorsConfig(
            background = darkestGrey,
            surface = darkGrey,
            foreground = lightestGrey,
            lightness = MaterialLightness.V400,
            variantLightness = MaterialLightness.V600,
            errorLightness = MaterialLightness.V800,
        )
    }
}
