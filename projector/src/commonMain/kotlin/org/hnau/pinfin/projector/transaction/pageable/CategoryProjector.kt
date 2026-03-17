package org.hnau.pinfin.projector.transaction.pageable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.transaction.pageable.CategoryModel
import org.hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.transaction.utils.ChooseOrCreateProjector
import org.hnau.pinfin.projector.utils.CategoryContent
import org.hnau.pinfin.projector.utils.ViewMode

class CategoryProjector(
    private val model: CategoryModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }


    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        viewMode: ViewMode = ViewMode.Full,
    ) {
        CategoryContent(
            info = model.category.collectAsState().value,
            modifier = modifier,
            selected = model.isFocused.collectAsState().value,
            onClick = model.requestFocus,
            localization = dependencies.localization,
            viewMode = viewMode,
        )
    }

    @Composable
    fun Content(
        selected: Boolean,
        onClick: (() -> Unit)?,
        modifier: Modifier = Modifier,
        viewMode: ViewMode = ViewMode.Full,
        content: @Composable (inner: @Composable () -> Unit) -> Unit = { inner -> inner() },
    ) {
        CategoryContent(
            info = model.category.collectAsState().value,
            modifier = modifier,
            selected = selected,
            onClick = onClick,
            viewMode = viewMode,
            content = content,
            localization = dependencies.localization,
        )
    }

    companion object {

        @Pipe
        interface Dependencies {

            val localization: Localization

            fun chooseOrCreate(): ChooseOrCreateProjector.Dependencies
        }

        fun createPage(
            model: ChooseOrCreateModel<CategoryInfo>,
            dependencies: Dependencies,
        ): ChooseOrCreateProjector<CategoryInfo> = ChooseOrCreateProjector(
            model = model,
            dependencies = dependencies.chooseOrCreate(),
        ) { category, isSelected, onClick ->
            CategoryContent(
                info = category,
                selected = isSelected.collectAsState().value,
                onClick = onClick,
                localization = dependencies.localization,
                viewMode = ViewMode.Full,
            )
        }
    }
}