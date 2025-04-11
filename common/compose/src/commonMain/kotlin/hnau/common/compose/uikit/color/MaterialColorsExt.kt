package hnau.common.compose.uikit.color

import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import hnau.common.color.RGBABytes
import hnau.common.color.material.MaterialHue
import hnau.common.color.material.MaterialLightness
import hnau.common.color.material.get
import hnau.common.color.material.material

val Colors.foregroundLightness: MaterialLightness
    get() = when (isLight) {
        true -> MaterialLightness.V800
        false -> MaterialLightness.V400
    }

@Composable
operator fun Colors.get(
    hue: MaterialHue,
): Color = RGBABytes
    .material[hue][foregroundLightness]
    .compose
