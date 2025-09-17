package hnau.pinfin.projector.transaction.pageable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.pinfin.model.transaction.pageable.CategoryModel
import hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.projector.transaction.utils.ChooseOrCreateProjector
import hnau.pinfin.projector.utils.CategoryContent
import hnau.pinfin.projector.utils.ViewMode
import kotlinx.coroutines.CoroutineScope

class CategoryProjector(
    scope: CoroutineScope,
    private val model: CategoryModel,
) {


    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        CategoryContent(
            info = model.category.collectAsState().value,
            modifier = modifier,
            selected = model.isFocused.collectAsState().value,
            onClick = model.requestFocus,
        )
    }

    @Composable
    fun Content(
        selected: Boolean,
        onClick: (() -> Unit)?,
        modifier: Modifier = Modifier,
        viewMode: ViewMode = ViewMode.default,
        content: @Composable (inner: @Composable () -> Unit) -> Unit = { inner -> inner() },
    ) {
        CategoryContent(
            info = model.category.collectAsState().value,
            modifier = modifier,
            selected = selected,
            onClick = onClick,
            viewMode = viewMode,
            content = content,
        )
    }

    companion object {

        fun createPage(
            scope: CoroutineScope,
            model: ChooseOrCreateModel<CategoryInfo>,
        ): ChooseOrCreateProjector<CategoryInfo> = ChooseOrCreateProjector(
            scope = scope,
            model = model,
        ) { category, isSelected, onClick ->
            CategoryContent(
                info = category,
                selected = isSelected.collectAsState().value,
                onClick = onClick,
            )
        }
    }
}