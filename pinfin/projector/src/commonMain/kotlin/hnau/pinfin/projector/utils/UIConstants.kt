package hnau.pinfin.projector.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object UIConstants {

    val absentValueIcon: ImageVector
        get() = Icons.AutoMirrored.Filled.Help

    val absentValueColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.error
}