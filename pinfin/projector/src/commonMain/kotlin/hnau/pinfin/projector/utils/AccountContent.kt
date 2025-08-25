package hnau.pinfin.projector.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import hnau.common.app.projector.uikit.ItemsRow
import hnau.common.app.projector.uikit.shape.HnauShape
import hnau.common.app.projector.utils.Icon
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.account
import hnau.pinfin.projector.resources.category
import org.jetbrains.compose.resources.stringResource

@Composable
fun SwitchHueToAccountInfo(
    info: AccountInfo,
    content: @Composable () -> Unit,
) {
    SwitchHue(
        hue = info.hue,
        content = content,
    )
}

@Composable
fun AccountContent(
    info: AccountInfo,
    modifier: Modifier = Modifier,
) {
    SwitchHueToAccountInfo(
        info = info,
    ) {
        Label(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            AccountContentInner(
                info = info,
                modifier = modifier,
            )
        }
    }
}

@Composable
fun AccountButton(
    info: AccountInfo?,
    modifier: Modifier = Modifier,
    selected: Boolean = true,
    shape: Shape = HnauShape(),
    onClick: (() -> Unit)?,
) {
    info.foldNullable(
        ifNull = {
            NotSelectedButton(
                onClick = onClick,
                shape = shape,
                modifier = modifier,
            ) {
                Text(
                    text = stringResource(Res.string.account),
                    maxLines = 1,
                )
            }
        },
        ifNotNull = { infoNotNull ->
            SwitchHueToAccountInfo(
                info = infoNotNull,
            ) {
                SelectButton(
                    modifier = modifier,
                    onClick = onClick,
                    selected = selected,
                    shape = shape,
                    content = {
                        AccountContentInner(
                            info = infoNotNull,
                        )
                    },
                )
            }
        }
    )
}

@Composable
private fun AccountContentInner(
    info: AccountInfo,
    modifier: Modifier = Modifier,
) {
    ItemsRow(
        modifier = modifier,
    ) {
        info.icon?.image?.let { icon -> Icon(icon) }
        Text(
            text = info.title,
            maxLines = 1,
        )
    }
}