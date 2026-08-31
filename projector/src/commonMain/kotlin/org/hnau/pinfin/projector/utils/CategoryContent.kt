package org.hnau.pinfin.projector.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import org.hnau.commons.kotlin.KeyValue
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
    EntityContent(
        entity = info,
        modifier = modifier,
        selected = selected,
        shape = shape,
        viewMode = viewMode,
        content = content,
        onClick = onClick,
        extractHue = { info -> info.value.hue },
        extractIcon = { info -> info.value.icon?.image },
        extractSuffixIcon = { info -> info.key.direction.icon },
        extractTitle = { info -> info.value.title },
        entityTypeName = localization.category,
    )
}