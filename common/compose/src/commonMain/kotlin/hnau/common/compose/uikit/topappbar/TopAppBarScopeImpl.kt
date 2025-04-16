package hnau.common.compose.uikit.topappbar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.HnauButton
import hnau.common.compose.uikit.table.CellBox
import hnau.common.compose.uikit.table.TableScope
import hnau.common.compose.uikit.table.cellShape
import hnau.common.compose.uikit.utils.Dimens

class TopAppBarScopeImpl(
    parent: TableScope,
) : TopAppBarScope, TableScope by parent {

    @Composable
    override fun Title(
        text: String,
    ) {
        CellBox(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                modifier = Modifier.padding(
                    horizontal = Dimens.separation,
                    vertical = Dimens.smallSeparation,
                ),
                text = text,
                maxLines = 1,
            )
        }
    }

    @Composable
    override fun Action(
        onClick: (() -> Unit)?,
        content: @Composable () -> Unit,
    ) {
        Cell {
            HnauButton(
                onClick = onClick,
                shape = cellShape,
                content = content,
            )
        }
    }
}