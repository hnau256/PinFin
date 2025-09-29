package hnau.pinfin.projector.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import arrow.core.NonEmptyList
import hnau.common.app.projector.uikit.row.ChipsFlowRow
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.collectAsMutableAccessor
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.ifFalse
import hnau.common.kotlin.it
import hnau.pinfin.model.filter.pageable.SelectCategoriesModel
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.categories
import hnau.pinfin.projector.utils.CategoryContent
import hnau.pinfin.projector.utils.Label
import org.jetbrains.compose.resources.stringResource

class SelectCategoriesProjector(
    private val model: SelectCategoriesModel,
) {

    class Page(
        private val model: SelectCategoriesModel.Page,
    ) {

        @Composable
        fun Content() {
            val categories: List<SelectCategoriesModel.Page.Category> by model.categories.collectAsState()
            ChipsFlowRow(
                all = categories,
            ) { item ->
                var selected by item.selected.collectAsMutableAccessor()
                CategoryContent(
                    info = item.info,
                    selected = selected,
                    onClick = { selected = !selected },
                )
            }
        }
    }

    @Composable
    fun Content() {
        val selectedCategories: NonEmptyList<CategoryInfo>? by model.selectedCategories.collectAsState()
        val hasSelectedCategories: Boolean = selectedCategories != null
        Label(
            selected = model.isFocused.collectAsState().value,
            onClick = model.requestFocus,
            containerColor = hasSelectedCategories.foldBoolean(
                ifTrue = { MaterialTheme.colorScheme.primaryContainer },
                ifFalse = { MaterialTheme.colorScheme.surfaceContainer },
            ),
        ) {
            Text(
                selectedCategories.foldNullable(
                    ifNull = { stringResource(Res.string.categories) },
                    ifNotNull = { categories ->
                        remember(categories) {
                            listOfNotNull(
                                categories
                                    .take(maxCount)
                                    .joinToString(transform = CategoryInfo::title),
                                categories
                                    .drop(maxCount)
                                    .size
                                    .takeIf { it > 0 }
                                    ?.let { "+$it" }
                            ).joinToString(
                                separator = " ",
                            )
                        }
                    }
                )
            )
        }
    }

    companion object {

        private const val maxCount: Int = 1
    }
}