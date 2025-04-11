package hnau.common.compose.uikit.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import hnau.common.compose.uikit.table.layout.TableLayout
import hnau.common.compose.uikit.utils.Dimens

@Composable
@NonSkippableComposable
fun Table(
    orientation: TableOrientation,
    modifier: Modifier = Modifier,
    parentCellScope: CellScope = CellScope.opened,
    content: @Composable TableScope.() -> Unit,
) {
    TableLayout(
        orientation = orientation,
        modifier = modifier,
        separationPx = with(LocalDensity.current) { Dimens.chipsSeparation.roundToPx() },
    ) {
        val scope = TableScopeImpl(
            parentCellScope = parentCellScope,
            orientation = orientation,
        )
        scope.content()
    }
}