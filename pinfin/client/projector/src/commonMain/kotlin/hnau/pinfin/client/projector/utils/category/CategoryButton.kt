package hnau.pinfin.client.projector.utils.category

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
import hnau.pinfin.client.data.budget.CategoryInfoResolver
import hnau.pinfin.scheme.CategoryId
import org.jetbrains.compose.resources.stringResource
import pinfin.pinfin.client.projector.generated.resources.Res
import pinfin.pinfin.client.projector.generated.resources.category

@Composable
fun CategoryButton(
    id: CategoryId?,
    infoResolver: CategoryInfoResolver,
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
            when (id) {
                null -> TripleRow(
                    content = { Text(stringResource(Res.string.category)) },
                    leading = { Icon { Icons.AutoMirrored.Filled.Help } },
                )

                else -> CategoryContent(
                    id = id,
                    infoResolver = infoResolver,
                )
            }
        },
        style = when (selected) {
            true -> ContainerStyle.Primary
            false -> when (id) {
                null -> ContainerStyle.Error
                else -> ContainerStyle.Neutral
            }
        }
    )
}