package hnau.common.android

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import hnau.common.compose.utils.DynamicColorsGenerator
import hnau.common.compose.utils.ThemeBrightness

class AndroidDynamicColorsGenerator private constructor(): DynamicColorsGenerator {

    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    override fun generateDynamicColors(
        brightness: ThemeBrightness
    ): androidx.compose.material3.ColorScheme {
        val context = LocalContext.current
        return when (brightness) {
            ThemeBrightness.Dark -> dynamicDarkColorScheme(context)
            ThemeBrightness.Light -> dynamicLightColorScheme(context)
        }
    }

    companion object {

        fun createIfSupported(): AndroidDynamicColorsGenerator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AndroidDynamicColorsGenerator()
        } else {
            null
        }
    }
}