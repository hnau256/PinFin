package hnau.pinfin.projector.transaction.pageable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.pinfin.model.transaction.pageable.CategoryModel
import hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.projector.transaction.utils.ChooseOrCreateProjector
import hnau.pinfin.projector.utils.CategoryButton
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class CategoryProjector(
    scope: CoroutineScope,
    private val model: CategoryModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        CategoryButton(
            info = model.category.collectAsState().value,
            modifier = modifier,
            selected = model.isFocused.collectAsState().value,
            onClick = model.requestFocus,
        )
    }

    @Composable
    fun ContentIcon(
        selected: Boolean,
        onClick: (() -> Unit)?,
        modifier: Modifier = Modifier,
    ) {
        CategoryButton(
            info = model.category.collectAsState().value,
            modifier = modifier,
            selected = selected,
            onClick = onClick,
            showTitle = false,
        )
    }

    companion object {

        fun createPage(
            scope: CoroutineScope,
            model: ChooseOrCreateModel<CategoryInfo>,
            dependencies: ChooseOrCreateProjector.Dependencies,
        ): ChooseOrCreateProjector<CategoryInfo> = ChooseOrCreateProjector(
            scope = scope,
            model = model,
            dependencies = dependencies,
        ) { category, isSelected, onClick ->
            CategoryButton(
                info = category,
                selected = isSelected.collectAsState().value,
                onClick = onClick,
            )
        }
    }
}