package hnau.pinfin.projector.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.pinfin.model.utils.budget.state.BudgetInfo

@Composable
fun BidgetInfoContent(
    info: BudgetInfo,
) {
    Box(
        modifier = Modifier.height(64.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = info.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}