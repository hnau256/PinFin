package hnau.pinfin.projector.utils

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ButtonDefaults.colors(
    container: Color,
    content: Color = contentColorFor(container),
): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = container,
    contentColor = content,
)