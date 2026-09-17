package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.hnau.commons.app.model.utils.fold
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.uikit.state.BooleanStateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.pinfin.model.transaction.edit.CategoryChooseModel
import org.hnau.pinfin.projector.Localization

class CategoryChooseEditProjector(
    private val model: CategoryChooseModel,
    private val localization: Localization,
) {

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        val isFocused = model.choose.navigateContext.isFocused.collectAsState().value
        val category = model.categoryEditable.collectAsState().value
        val categoryInfo = category.fold(
            ifIncorrect = { null },
            ifValue = { value, _ -> value.value },
        )

        SItem(
            modifier = modifier,
            topAccessory = {
                SText(localization.category)
            },
        ) {
            isFocused.BooleanStateContent(
                transitionSpec = TransitionSpec.rememberCrossfade(),
                falseContent = {
                    ReadOnlyValue(
                        text = categoryInfo?.title ?: localization.category,
                        onClick = model.choose.navigateContext.requestFocus,
                    )
                },
                trueContent = {
                    ChooseOrCreateContent(model = model.choose) { idWithCategory ->
                        SText(idWithCategory.value.title)
                    }
                },
            )
        }
    }
}
