package hnau.pinfin.projector.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import hnau.common.projector.uikit.ContainerStyle
import hnau.common.projector.uikit.HnauButton
import hnau.common.projector.uikit.table.CellBox
import hnau.common.projector.uikit.table.Subtable
import hnau.common.projector.uikit.table.Table
import hnau.common.projector.uikit.table.TableOrientation
import hnau.common.projector.uikit.table.TableScope
import hnau.common.projector.uikit.table.cellShape
import hnau.common.projector.uikit.utils.Dimens

@Composable
fun Dialog(
    title: String,
    buttons: @Composable DialogButtonsScope.() -> Unit,
) {
    Table(
        orientation = TableOrientation.Vertical,
    ) {
        CellBox {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(Dimens.separation),
            )
        }
        Subtable {
            val buttonsScope = DialogButtonsScopeImpl.remember(
                tableScope = this,
            )
            buttonsScope.buttons()
        }
    }
}

interface DialogButtonsScope : TableScope {

    @Composable
    fun Button(
        text: String,
        style: ContainerStyle,
        onClick: () -> Unit,
    )
}

private class DialogButtonsScopeImpl(
    tableScope: TableScope,
) : TableScope by tableScope, DialogButtonsScope {

    @Composable
    override fun Button(
        text: String,
        style: ContainerStyle,
        onClick: () -> Unit,
    ) {
        Cell {
            HnauButton(
                modifier = Modifier.weight(1f),
                shape = cellShape,
                style = style,
                onClick = onClick,
                content = { Text(text) }
            )
        }
    }

    companion object {

        @Composable
        fun remember(
            tableScope: TableScope,
        ): DialogButtonsScope = remember(tableScope) {
            DialogButtonsScopeImpl(
                tableScope = tableScope,
            )
        }
    }

}