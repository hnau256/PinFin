package hnau.pinfin.projector.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.common.compose.uikit.state.LoadableContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.kotlin.Loadable
import hnau.pinfin.model.utils.budget.state.BudgetInfo

@Composable
fun BidgetInfoLoadableContent(
    info: Loadable<BudgetInfo>,
) {
    Box(
        modifier = Modifier.height(64.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        info.LoadableContent(
            transitionSpec = TransitionSpec.crossfade(),
            loadingContent = {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                )
            }
        ) { info ->
            Text(
                text = info.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}