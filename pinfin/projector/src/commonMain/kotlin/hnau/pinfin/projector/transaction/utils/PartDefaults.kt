package hnau.pinfin.projector.transaction.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object PartDefaults {

    val background: Color
        @Composable
        get() = MaterialTheme.colorScheme.surfaceContainer

    val outlinedTextFieldColors: TextFieldColors
        @Composable
        get() = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = background,
            focusedBorderColor = contentColorFor(background),
            focusedContainerColor = background,
            unfocusedContainerColor = background,
        )
}