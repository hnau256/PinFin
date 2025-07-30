package hnau.pinfin.projector.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import hnau.common.app.projector.uikit.shape.HnauShape
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.model
import hnau.pinfin.projector.resources.Res
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
        AccountContentInner(
            info = info,
            modifier = modifier,
        )
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
                Text(stringResource(Res.string.category))
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
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.extraSmallSeparation),
    ) {
        info.icon?.image?.let { icon ->
            Icon(icon)
        }
        Text(
            text = info.title,
            maxLines = 1,
        )
    }
}