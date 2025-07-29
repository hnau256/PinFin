package hnau.pinfin.projector.utils.account

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import hnau.common.app.projector.uikit.shape.HnauShape
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.category
import hnau.pinfin.projector.utils.NotSelectedButton
import hnau.pinfin.projector.utils.SelectButton
import hnau.pinfin.projector.utils.SwitchHueToAccountInfo
import hnau.pinfin.projector.utils.SwitchHueToCategoryInfo
import hnau.pinfin.projector.utils.category.CategoryContent
import org.jetbrains.compose.resources.stringResource

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
                        AccountContent(
                            info = infoNotNull,
                        )
                    },
                )
            }
        }
    )
}