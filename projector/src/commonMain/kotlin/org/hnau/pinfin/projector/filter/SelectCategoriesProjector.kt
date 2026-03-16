package org.hnau.pinfin.projector.filter

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import arrow.core.NonEmptyList
import org.hnau.commons.app.projector.uikit.row.ChipsFlowRow
import org.hnau.commons.app.projector.utils.collectAsMutableAccessor
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.model.filter.pageable.SelectCategoriesModel
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.CategoryContent
import org.hnau.pinfin.projector.utils.Label

class SelectCategoriesProjector(
    private val model: SelectCategoriesModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    class Page(
        private val model: SelectCategoriesModel.Page,
        private val dependencies: Dependencies,
    ) {

        @Pipe
        interface Dependencies {

            val localization: Localization
        }

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
                    localization = dependencies.localization,
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
                    ifNull = { (dependencies.localization.categories) },
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