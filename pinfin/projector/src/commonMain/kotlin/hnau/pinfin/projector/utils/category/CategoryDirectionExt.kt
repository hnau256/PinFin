package hnau.pinfin.projector.utils.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.ui.graphics.vector.ImageVector
import hnau.pinfin.data.AmountDirection

val AmountDirection.icon: ImageVector
    get() = when (this) {
        AmountDirection.Credit -> Icons.Filled.AddCircle
        AmountDirection.Debit -> Icons.Filled.DoNotDisturbOn
    }