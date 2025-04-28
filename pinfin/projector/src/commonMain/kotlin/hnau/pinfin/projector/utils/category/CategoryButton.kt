package hnau.pinfin.projector.utils.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import hnau.common.compose.uikit.ContainerStyle
import hnau.common.compose.uikit.HnauButton
import hnau.common.compose.uikit.TripleRow
import hnau.common.compose.uikit.shape.HnauShape
import hnau.common.compose.utils.Icon
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.jetbrains.compose.resources.stringResource
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.category

@Composable
fun CategoryButton(
    info: CategoryInfo?,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    shape: Shape = HnauShape(),
    onClick: (() -> Unit)?,
) {
    HnauButton(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        content = {
            when (info) {
                null -> TripleRow(
                    content = { Text(stringResource(Res.string.category)) },
                    leading = { Icon { Icons.AutoMirrored.Filled.Help } },
                )

                else -> CategoryContent(
                    info = info,
                )
            }
        },
        style = when (selected) {
            true -> ContainerStyle.Primary
            false -> when (info) {
                null -> ContainerStyle.Error
                else -> ContainerStyle.Neutral
            }
        }
    )
}