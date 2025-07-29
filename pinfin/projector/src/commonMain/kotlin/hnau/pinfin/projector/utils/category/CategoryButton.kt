package hnau.pinfin.projector.utils.category

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import hnau.common.app.projector.uikit.shape.HnauShape
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.category
import hnau.pinfin.projector.utils.NotSelectedButton
import hnau.pinfin.projector.utils.SelectButton
import hnau.pinfin.projector.utils.SwitchHueToCategoryInfo
import org.jetbrains.compose.resources.stringResource

@Composable
fun CategoryButton(
    info: CategoryInfo?,
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
            SwitchHueToCategoryInfo(
                info = infoNotNull,
            ) {
                SelectButton(
                    modifier = modifier,
                    onClick = onClick,
                    selected = selected,
                    shape = shape,
                    content = {
                        CategoryContent(
                            info = infoNotNull,
                        )
                    },
                )
            }
        }
    )
}