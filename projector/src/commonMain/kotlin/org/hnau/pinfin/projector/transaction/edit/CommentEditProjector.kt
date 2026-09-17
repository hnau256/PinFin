package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.distance.LocalDistance
import org.hnau.commons.app.projector.fractal.size.units
import org.hnau.commons.app.projector.uikit.state.BooleanStateContent
import org.hnau.commons.app.projector.uikit.state.NullableStateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.fold
import org.hnau.pinfin.model.transaction.edit.CommentEditModel
import org.hnau.pinfin.projector.Localization

class CommentEditProjector(
    private val model: CommentEditModel,
    private val localization: Localization,
) {

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        val isFocused = model.navigateContext.isFocused.collectAsState().value
        val input = model.input.collectAsState().value
        val suggestsLoadable = model.suggests.collectAsState().value

        SItem(
            modifier = modifier,
            topAccessory = {
                SText(localization.comment)
            },
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(LocalDistance.current.units.padding.along.small),
            ) {
                val suggests = suggestsLoadable
                    ?.fold(
                        ifLoading = { emptyList() },
                        ifReady = { it.value },
                    )
                suggests.NullableStateContent(
                    transitionSpec = TransitionSpec.remember(
                        showAlignment = Alignment.BottomCenter,
                    ),
                ) { suggestsNotNull ->
                    ChipsRow(items = suggestsNotNull) { suggest ->
                        SPanel(
                            actionOrElseOrDisabled = ActionOrElse.instant(suggest.onClick),
                        ) {
                            SText(suggest.comment.text)
                        }
                    }
                }
                isFocused.BooleanStateContent(
                    transitionSpec = TransitionSpec.rememberCrossfade(),
                    falseContent = {
                        ReadOnlyValue(
                            text = input.ifEmpty { localization.comment },
                            onClick = model.navigateContext.requestFocus,
                        )
                    },
                    trueContent = {
                        EditTextField(
                            value = input,
                            onValueChanged = { model.input.value = it },
                            navigateContext = model.navigateContext,
                        )
                    },
                )
            }
        }
    }
}
