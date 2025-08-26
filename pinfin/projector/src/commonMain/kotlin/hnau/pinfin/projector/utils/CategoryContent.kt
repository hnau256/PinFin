package hnau.pinfin.projector.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import hnau.common.app.projector.uikit.ItemsRow
import hnau.common.app.projector.uikit.shape.HnauShape
import hnau.common.app.projector.utils.Icon
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.utils.budget.state.CategoryInfo
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
    viewMode: CategoryViewMode = CategoryViewMode.default,
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
                viewMode = viewMode,
            )
        }
    }
}

@Composable
fun CategoryButton(
    info: CategoryInfo?,
    modifier: Modifier = Modifier,
    viewMode: CategoryViewMode = CategoryViewMode.default,
    selected: Boolean = true,
    shape: Shape = HnauShape(),
    onClick: (() -> Unit)?,
) {
    info.foldNullable(
        ifNull = {
            //TODO check viewMode
            NotSelectedButton(
                onClick = onClick,
                shape = shape,
                modifier = modifier,
            ) {
                Text(
                    text = stringResource(Res.string.category),
                    maxLines = 1,
                )
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
                            viewMode = viewMode,
                        )
                    },
                )
            }
        }
    )
}


enum class CategoryViewMode {
    Full, Icon;

    companion object {

        val default: CategoryViewMode
            get() = Full
    }
}

@Composable
private fun CategoryContentInner(
    info: CategoryInfo,
    modifier: Modifier = Modifier,
    viewMode: CategoryViewMode,
) {
    when (viewMode) {
        CategoryViewMode.Full -> CategoryContentFullInner(
            info = info,
            modifier = modifier,
        )

        CategoryViewMode.Icon -> CategoryContentIconInner(
            info = info,
            modifier = modifier,
        )
    }
}

@Composable
private fun CategoryContentFullInner(
    info: CategoryInfo,
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

@Composable
private fun CategoryContentIconInner(
    info: CategoryInfo,
    modifier: Modifier = Modifier,
) {
    info
        .icon
        .foldNullable(
            ifNotNull = { icon ->
                Icon(
                    modifier = modifier,
                    icon = icon.image,
                )
            },
            ifNull = {
                Text(
                    modifier = modifier,
                    text = info.title.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            },
        )
}