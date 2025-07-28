package hnau.pinfin.projector.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import hnau.common.app.projector.uikit.ContainerStyle
import hnau.common.app.projector.uikit.HnauButton
import hnau.common.app.projector.uikit.table.CellBox
import hnau.common.app.projector.uikit.table.Subtable
import hnau.common.app.projector.uikit.table.Table
import hnau.common.app.projector.uikit.table.TableOrientation
import hnau.common.app.projector.uikit.utils.Dimens
import kotlinx.collections.immutable.ImmutableList

data class DialogButton(
    val text: @Composable () -> String,
    val style: ContainerStyle,
    val onClick: () -> Unit,
)

@Composable
fun Dialog(
    title: String,
    buttons: ImmutableList<DialogButton>,
) {
    Table(
        orientation = TableOrientation.Vertical,
        modifier = Modifier.fillMaxWidth(),
    ) {
        CellBox(
            isLast = false,
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(Dimens.separation),
            )
        }
        Subtable(
            isLast = true,
        ) {
            buttons
                .forEachIndexed { i, button ->
                    Cell(
                        isLast = i == buttons.lastIndex,
                    ) { modifier ->
                        HnauButton(
                            modifier = modifier.weight(1f),
                            shape = shape,
                            style = button.style,
                            onClick = button.onClick,
                            content = { Text(button.text()) },
                        )
                    }
                }
        }
    }
}