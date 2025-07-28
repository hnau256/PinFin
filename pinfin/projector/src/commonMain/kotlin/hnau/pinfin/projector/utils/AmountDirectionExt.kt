package hnau.pinfin.projector.utils

import androidx.compose.ui.graphics.Color
import dynamiccolor.Variant
import hnau.common.app.model.theme.Hue
import hnau.common.app.model.theme.ThemeBrightness
import hnau.common.app.projector.uikit.ContainerStyle
import hnau.common.app.projector.utils.system
import hnau.common.app.projector.utils.theme.DynamicScheme
import hnau.pinfin.data.AmountDirection

val AmountDirection.hue: Hue
    get() = when (this) {
        AmountDirection.Credit -> Hue(0.375f)
        AmountDirection.Debit -> Hue(0.075f)
    }

private val containerStyles: MutableMap<AmountDirection, MutableMap<ThemeBrightness, ContainerStyle.Colors>> =
    HashMap()

val AmountDirection.containerStyle: ContainerStyle
    get() = containerStyles
        .getOrPut(this) { HashMap() }
        .let { styles ->
            ContainerStyle {
                val brightness = ThemeBrightness.system
                styles.getOrPut(brightness) {
                    val scheme = DynamicScheme(
                        primaryHue = hue,
                        brightness = brightness,
                        variant = Variant.FIDELITY,
                    )
                    ContainerStyle.Colors(
                        container = Color(scheme.primaryContainer),
                        content = Color(scheme.onPrimaryContainer),
                    )
                }
            }
        }