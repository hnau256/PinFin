package hnau.pinfin.projector.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import hnau.pinfin.data.CategoryDirection

fun CategoryDirection.getColor(
    colors: ColorScheme,
): Color = when (this) {
    CategoryDirection.Credit -> colors.primary
    CategoryDirection.Debit -> colors.error
}

val CategoryDirection.color: Color
    @Composable
    get() = getColor(
        colors = MaterialTheme.colorScheme,
    )