package hnau.pinfin.projector.utils.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.ui.graphics.vector.ImageVector
import hnau.pinfin.data.CategoryDirection

val CategoryDirection.icon: ImageVector
    get() = when (this) {
        CategoryDirection.Credit -> Icons.Filled.AddCircle
        CategoryDirection.Debit -> Icons.Filled.DoNotDisturbOn
    }