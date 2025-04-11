package hnau.pinfin.client.projector.transaction.type.utils

import androidx.compose.runtime.Composable
import hnau.pinfin.client.data.budget.CategoryInfoResolver
import hnau.pinfin.client.model.transaction.type.utils.ChooseCategoryModel
import hnau.pinfin.client.projector.utils.category.CategoryButton
import hnau.pinfin.client.projector.utils.choose.Content
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class ChooseCategoryProjector(
    private val scope: CoroutineScope,
    private val model: ChooseCategoryModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        val categoryInfoResolver: CategoryInfoResolver
    }

    @Composable
    fun Content() {
        model
            .state
            .Content { id, selected, onClick ->
                CategoryButton(
                    id = id,
                    onClick = onClick,
                    selected = selected,
                    infoResolver = dependencies.categoryInfoResolver,
                )
            }
    }
}