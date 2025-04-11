package hnau.pinfin.client.projector.utils.category

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.TripleRow
import hnau.common.compose.utils.Icon
import hnau.pinfin.client.data.budget.CategoryInfoResolver
import hnau.pinfin.client.projector.utils.color
import hnau.pinfin.scheme.CategoryId

@Composable
fun CategoryContent(
    id: CategoryId,
    infoResolver: CategoryInfoResolver,
    modifier: Modifier = Modifier,
) {
    val info = infoResolver[id]
    CompositionLocalProvider(
        LocalContentColor provides id.direction.color
    ) {
        TripleRow(
            modifier = modifier,
            content = { Text(info.title) },
            leading = { Icon { id.direction.icon } }
        )
    }
}