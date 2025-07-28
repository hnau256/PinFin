package hnau.pinfin.projector.utils.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import hnau.common.app.projector.uikit.ContainerStyle
import hnau.common.app.projector.uikit.HnauButton
import hnau.common.app.projector.uikit.TripleRow
import hnau.common.app.projector.uikit.shape.HnauShape
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.category
import org.jetbrains.compose.resources.stringResource

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
                    leading = { Icon(Icons.AutoMirrored.Filled.Help) },
                )

                else -> CategoryContent(
                    info = info,
                )
            }
        },
        style = when (selected) {
            true -> ContainerStyle.primary
            false -> when (info) {
                null -> ContainerStyle.error
                else -> ContainerStyle.neutral
            }
        }
    )
}