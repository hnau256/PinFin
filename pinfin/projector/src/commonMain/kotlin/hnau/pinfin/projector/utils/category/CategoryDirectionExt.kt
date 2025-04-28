package hnau.pinfin.projector.utils.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.ui.graphics.vector.ImageVector
import hnau.pinfin.data.CategoryDirection

val CategoryDirection.icon: ImageVector
    get() = when (this) {
        CategoryDirection.Credit -> Icons.Filled.ArrowCircleUp
        CategoryDirection.Debit -> Icons.Filled.ArrowCircleDown
    }