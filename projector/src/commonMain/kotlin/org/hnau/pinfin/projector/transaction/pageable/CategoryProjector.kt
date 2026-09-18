package org.hnau.pinfin.projector.transaction.pageable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.transaction.pageable.CategoryModel
import org.hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import org.hnau.pinfin.model.utils.budget.state.CategoryIdWithInfo
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.transaction.utils.ChooseOrCreateProjector
import org.hnau.pinfin.projector.utils.EntityContent
import org.hnau.pinfin.projector.utils.ViewMode
import org.hnau.pinfin.projector.utils.rememberEntityUiInfo

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
        EntityContent(
            uiInfo = model
                .category
                .collectAsState()
                .value
                .rememberEntityUiInfo(
                    localization = dependencies.localization,
                ),
            modifier = modifier,
            selected = model.isFocused.collectAsState().value,
            onClick = model.requestFocus,
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
        EntityContent(
            uiInfo = model
                .category
                .collectAsState()
                .value
                .rememberEntityUiInfo(
                    localization = dependencies.localization,
                ),
            modifier = modifier,
            selected = selected,
            onClick = onClick,
            viewMode = viewMode,
            content = content,
        )
    }

    companion object {

        @Pipe
        interface Dependencies {

            val localization: Localization

            fun chooseOrCreate(): ChooseOrCreateProjector.Dependencies
        }

        fun createPage(
            model: ChooseOrCreateModel<CategoryIdWithInfo>,
            dependencies: Dependencies,
        ): ChooseOrCreateProjector<CategoryIdWithInfo> = ChooseOrCreateProjector(
            model = model,
            dependencies = dependencies.chooseOrCreate(),
        ) { category, isSelected, onClick ->
            EntityContent(
                uiInfo = category.rememberEntityUiInfo(
                    localization = dependencies.localization,
                ),
                selected = isSelected.collectAsState().value,
                onClick = onClick,
                viewMode = ViewMode.Full,
            )
        }
    }
}