package hnau.pinfin.projector.transaction.type.utils

import androidx.compose.runtime.Composable
import hnau.pinfin.model.transaction.type.utils.ChooseCategoryModel
import hnau.pinfin.projector.utils.category.CategoryButton
import hnau.pinfin.projector.utils.choose.Content
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class ChooseCategoryProjector(
    private val scope: CoroutineScope,
    private val model: ChooseCategoryModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies

    @Composable
    fun Content() {
        model
            .state
            .Content { info, selected, onClick ->
                CategoryButton(
                    info = info,
                    onClick = onClick,
                    selected = selected,
                )
            }
    }
}