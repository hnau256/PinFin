package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SLine
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.distance.LocalDistance
import org.hnau.commons.app.projector.fractal.size.units
import org.hnau.commons.app.projector.uikit.state.BooleanStateContent
import org.hnau.commons.app.projector.uikit.state.NullableStateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.instant
import org.hnau.commons.kotlin.fold
import org.hnau.pinfin.model.transaction.edit.CommentEditModel
import org.hnau.pinfin.projector.Localization

class CommentEditProjector(
    private val model: CommentEditModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        val input by model.input.collectAsState()

        SFocusablePanel(
            modifier = modifier,
            navigateContext = model.navigateContext,
        ) { isFocused ->
            SItem(
                topAccessory = {
                    SText(dependencies.localization.comment)
                },
            ) {
                SLine(
                    orientation = Orientation.Vertical,
                ) {
                    isFocused.BooleanStateContent(
                        transitionSpec = TransitionSpec.remember(
                            showAlignment = Alignment.BottomStart,
                            hideAlignment = Alignment.BottomStart,
                        )
                    ) {
                        LazyRow {  }
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(LocalDistance.current.units.padding.along.small),
                ) {

                    val suggestsLoadable by model.suggests.collectAsState()
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
                    EditTextField(
                        value = input,
                        onValueChanged = { model.input.value = it },
                        navigateContext = model.navigateContext,
                    )
                }
            }
        }
    }
}
