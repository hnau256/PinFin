package hnau.common.compose.utils

import androidx.compose.material3.ColorScheme
import hnau.common.color.material.MaterialHue

expect fun buildPrettyColorScheme(
    primaryHue: MaterialHue,
    brightness: ThemeBrightness,
): ColorScheme