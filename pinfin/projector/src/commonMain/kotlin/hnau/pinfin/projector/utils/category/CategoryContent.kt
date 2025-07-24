package hnau.pinfin.projector.utils.category

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hnau.pinfin.model.utils.budget.state.CategoryInfo

@Composable
fun CategoryContent(
    info: CategoryInfo,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = info.title,
    )
}