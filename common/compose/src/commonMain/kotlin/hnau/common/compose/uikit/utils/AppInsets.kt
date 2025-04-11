package hnau.common.compose.uikit.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable

val appInsets: PaddingValues
    @Composable
    get() = PaddingValues()// WindowInsets.safeDrawing.asPaddingValues()