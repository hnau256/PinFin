package org.hnau.pinfin.projector.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import org.hnau.commons.app.projector.utils.rememberRun
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.pinfin.projector.Localization

@Composable
fun CategoryContent(
    info: KeyValue<CategoryId, CategoryInfo>?,
    localization: Localization,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    shape: Shape = LabelDefaults.shape,
    viewMode: ViewMode = ViewMode.default,
    onClick: (() -> Unit)? = null,
    content: @Composable (inner: @Composable () -> Unit) -> Unit = { inner -> inner() },
) {
    val directionIcon = info?.key?.direction?.icon
    EntityContent(
        uiInfo = info?.rememberRun(directionIcon) {
            EntityUiInfo(
                hue = value.hue,
                icon = value.icon?.image.foldNullable(
                    ifNull = {
                        directionIcon?.let { directionIcon ->
                            EntityUiInfo.Icon(
                                main = directionIcon,
                            )
                        }
                    },
                    ifNotNull = { icon ->
                        EntityUiInfo.Icon(
                            main = icon,
                            additional = directionIcon,
                        )
                    }
                ),
                title = value.title,
            )
        },
        modifier = modifier,
        selected = selected,
        shape = shape,
        viewMode = viewMode,
        content = content,
        onClick = onClick,
        entityTypeName = localization.category,
    )
}