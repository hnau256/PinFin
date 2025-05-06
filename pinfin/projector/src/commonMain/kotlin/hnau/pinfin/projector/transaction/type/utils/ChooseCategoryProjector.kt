package hnau.pinfin.projector.transaction.type.utils

import androidx.compose.runtime.Composable
import hnau.pinfin.model.transaction.type.utils.ChooseCategoryModel
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.categories_not_found
import hnau.pinfin.projector.create_new_category
import hnau.pinfin.projector.there_are_no_categories
import hnau.pinfin.projector.utils.category.CategoryButton
import hnau.pinfin.projector.utils.choose.ChooseMessages
import hnau.pinfin.projector.utils.choose.Content
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

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
            .Content(
                messages = ChooseMessages(
                    createNew = stringResource(Res.string.create_new_category),
                    notFound = stringResource(Res.string.categories_not_found),
                    noVariants = stringResource(Res.string.there_are_no_categories),
                ),
            ) { info, selected, onClick ->
                CategoryButton(
                    info = info,
                    onClick = onClick,
                    selected = selected,
                )
            }
    }
}