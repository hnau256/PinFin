package hnau.common.compose.uikit

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

enum class ContainerStyle {
    Neutral,
    Primary,
    Secondary,
    Tertiary,
    Error,
    ;

    data class Style(
        val container: Color,
        val content: Color,
    )

    @Composable
    fun rememberStyle(): Style {
        val colors = MaterialTheme.colorScheme
        return remember(this, colors) {
            when (this) {
                Neutral -> Style(
                    container = colors.surfaceContainer,
                    content = colors.onSurface,
                )

                Primary -> Style(
                    container = colors.primaryContainer,
                    content = colors.onPrimaryContainer,
                )

                Secondary -> Style(
                    container = colors.secondaryContainer,
                    content = colors.onSecondaryContainer,
                )

                Tertiary -> Style(
                    container = colors.tertiaryContainer,
                    content = colors.onTertiaryContainer,
                )

                Error -> Style(
                    container = colors.errorContainer,
                    content = colors.onErrorContainer,
                )
            }
        }
    }
}