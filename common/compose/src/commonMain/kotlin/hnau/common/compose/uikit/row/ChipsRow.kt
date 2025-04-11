package hnau.common.compose.uikit.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import hnau.common.compose.uikit.shape.HnauShape
import hnau.common.compose.uikit.shape.create
import hnau.common.compose.uikit.shape.inRow
import hnau.common.compose.uikit.utils.Dimens

@Composable
fun <T> ChipsRow(
    all: List<T>,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable (item: T, shape: Shape) -> Unit,
) = Row(
    modifier = modifier,
    verticalAlignment = verticalAlignment,
    horizontalArrangement = Arrangement.spacedBy(
        space = Dimens.chipsSeparation,
        alignment = horizontalAlignment,
    ),
) {
    val totalCount = all.size
    all.forEachIndexed { i, item ->
        val shape = HnauShape.inRow.create(
            totalCount = totalCount,
            index = i,
        )
        content(item, shape)
    }
}
