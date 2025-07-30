package hnau.pinfin.projector.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import hnau.common.app.projector.uikit.TripleRow
import hnau.common.app.projector.uikit.shape.HnauShape
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.model
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.category
import org.jetbrains.compose.resources.stringResource

@Composable
fun SwitchHueToCategoryInfo(
    info: CategoryInfo,
    content: @Composable () -> Unit,
) {
    SwitchHue(
        hue = info.hue,
        content = content,
    )
}

@Composable
fun CategoryContent(
    info: CategoryInfo,
    modifier: Modifier = Modifier,
) {
    SwitchHueToCategoryInfo(
        info = info,
    ) {
        Label(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            CategoryContentInner(
                info = info,
                modifier = modifier,
            )
        }
    }
}

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
                        CategoryContentInner(
                            info = infoNotNull,
                        )
                    },
                )
            }
        }
    )
}

@Composable
private fun CategoryContentInner(
    info: CategoryInfo,
    modifier: Modifier = Modifier,
) {
    TripleRow(
        modifier = modifier,
        leading = info.icon?.image?.let { icon -> { Icon(icon) } },
        content = {
            Text(
                text = info.title,
                maxLines = 1,
                )
        }
    )
}