package hnau.pinfin.projector.utils.category

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.TripleRow
import hnau.common.compose.utils.Icon
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.projector.utils.color

@Composable
fun CategoryContent(
    info: CategoryInfo,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(
        LocalContentColor provides info.id.direction.color
    ) {
        TripleRow(
            modifier = modifier,
            content = { Text(info.title) },
            leading = { Icon(info.id.direction.icon) }
        )
    }
}