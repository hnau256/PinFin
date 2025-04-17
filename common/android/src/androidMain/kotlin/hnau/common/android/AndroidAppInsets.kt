package hnau.common.android

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable

object AndroidAppInsets : AppInsets {

    override val insets: PaddingValues
        @Composable
        get() = WindowInsets.safeDrawing.asPaddingValues()
}