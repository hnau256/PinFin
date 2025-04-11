package hnau.common.compose.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

enum class ThemeBrightness {
    Light,
    Dark,
    ;

    companion object {

        val system: ThemeBrightness
            @Composable
            get() = when (isSystemInDarkTheme()) {
                true -> Dark
                false -> Light
            }
    }
}