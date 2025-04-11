package hnau.pinfin.client.compose.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import hnau.common.color.material.MaterialHue

@Composable
fun pinfinTheme(
    content: @Composable () -> Unit,
) = MaterialTheme(
    colors = buildColors(
        primaryHue = MaterialHue.Amber,
        secondaryHue = MaterialHue.Teal,
        isDark = isSystemInDarkTheme(),
    ),
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colors.onBackground,
        ) {
            content()
        }
    }
}
