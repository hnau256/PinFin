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
import hnau.common.projector.uikit.table.Cell
import hnau.common.projector.uikit.table.CellBox
import hnau.common.projector.uikit.table.Subtable
import hnau.common.projector.uikit.table.Table
import hnau.common.projector.uikit.table.TableOrientation
import hnau.common.projector.uikit.utils.Dimens
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

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
        cells = remember(title, buttons) {
            persistentListOf(
                CellBox {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(Dimens.separation),
                    )
                },
                Subtable(
                    cells = buttons
                        .map { button ->
                            Cell {
                                HnauButton(
                                    modifier = Modifier.weight(1f),
                                    shape = shape,
                                    style = button.style,
                                    onClick = button.onClick,
                                    content = { Text(button.text()) }
                                )
                            }
                        }
                        .toImmutableList()
                )
            )
        }
    )
}