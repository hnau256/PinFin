package hnau.pinfin.projector.utils.account

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
import hnau.pinfin.data.budget.AccountInfo
import org.jetbrains.compose.resources.stringResource
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.account

@Composable
fun AccountButton(
    info: AccountInfo?,
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
                    content = { Text(stringResource(Res.string.account)) },
                    leading = { Icon { Icons.AutoMirrored.Filled.Help } },
                )

                else -> AccountContent(
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