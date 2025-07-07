package hnau.pinfin.projector.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import hnau.pinfin.data.AmountDirection

fun AmountDirection.getColor(
    colors: ColorScheme,
): Color = when (this) {
    AmountDirection.Credit -> colors.primary
    AmountDirection.Debit -> colors.error
}

val AmountDirection.color: Color
    @Composable
    get() = getColor(
        colors = MaterialTheme.colorScheme,
    )