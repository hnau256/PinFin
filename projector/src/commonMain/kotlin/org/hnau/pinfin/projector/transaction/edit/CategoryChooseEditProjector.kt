package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.transaction.edit.CategoryChooseModel
import org.hnau.pinfin.model.transaction.edit.utils.chooseorcreate.BaseOrAdditionalValues
import org.hnau.pinfin.model.utils.budget.state.CategoryIdWithInfo
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.transaction.edit.utils.chooseorcreate.ChooseOrCreateMessages
import org.hnau.pinfin.projector.transaction.edit.utils.chooseorcreate.ChooseOrCreateProjector
import org.hnau.pinfin.projector.utils.chipInfo

class CategoryChooseEditProjector(
    scope: CoroutineScope,
    model: CategoryChooseModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun chooseOrCreate(): ChooseOrCreateProjector.Dependencies
    }

    private val delegate: ChooseOrCreateProjector<CategoryIdWithInfo> = ChooseOrCreateProjector(
        scope = scope,
        model = model.choose,
        dependencies = dependencies.chooseOrCreate(),
        extractChipInfo = CategoryIdWithInfo::chipInfo,
        messages = dependencies
            .localization
            .run {
                ChooseOrCreateMessages(
                    entityTypeName = category,
                    sections = BaseOrAdditionalValues(
                        base = ChooseOrCreateMessages.Section(
                            title = chooseCategoryVariantsTitle,
                            noVariantsNoInput = chooseCategoryVariantsEmptyNoInput,
                            noVariantsWithInput = chooseCategoryVariantsEmptyInput,
                        ),
                        additional = ChooseOrCreateMessages.Section(
                            title = chooseCategoryAdditionalVariantsTitle,
                            noVariantsNoInput = chooseCategoryAdditionalVariantsEmptyNoInput,
                            noVariantsWithInput = chooseCategoryAdditionalVariantsEmptyInput,
                        ),
                    )
                )
            },
    )

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        delegate.Content(
            modifier = modifier,
        )
    }
}
