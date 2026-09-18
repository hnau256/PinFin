package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.core.toNonEmptyListOrNull
import org.hnau.commons.app.projector.fractal.SButton
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SLine
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.STitleOrIcon
import org.hnau.commons.app.projector.fractal.distance.LocalDistance
import org.hnau.commons.app.projector.fractal.padding.LocalContentPadding
import org.hnau.commons.app.projector.fractal.size.scale
import org.hnau.commons.app.projector.fractal.size.units
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTable
import org.hnau.commons.app.projector.uikit.state.BooleanStateContent
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.state.NullableStateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.app.projector.utils.rememberLet
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.instant
import org.hnau.commons.kotlin.fold
import org.hnau.pinfin.model.transaction.edit.CommentEditModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.EntityContent
import org.hnau.pinfin.projector.utils.EntityUiInfo
import org.hnau.pinfin.projector.utils.Label

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
                        ),
                    ) {
                        val loadableSuggests by model.suggests.collectAsState()
                        loadableSuggests.LoadableContent(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp.scale(LocalDistance.current.scale.space))
                                .padding(
                                    bottom = LocalDistance.current.units.padding.along.small,
                                ),
                            transitionSpec = TransitionSpec.rememberCrossfade(),
                            loadingContent = {},
                        ) { suggestsOrEmpty ->
                            suggestsOrEmpty
                                .value
                                .toNonEmptyListOrNull()
                                .NullableStateContent(
                                    modifier = Modifier.fillMaxWidth(),
                                    transitionSpec = TransitionSpec.rememberCrossfade(),
                                    nullContent = {
                                        Box(
                                            contentAlignment = Alignment.CenterStart,
                                        ) {
                                            SText(
                                                text = dependencies.localization.noSuggests,
                                            )
                                        }
                                    }
                                ) { suggests ->
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = LocalContentPadding.current,
                                        horizontalArrangement = Arrangement.spacedBy(LocalDistance.current.units.padding.along.small),
                                    ) {
                                        items(
                                            items = suggests,
                                            key = { it.comment.text },
                                        ) { suggest ->
                                            SButton(
                                                modifier = Modifier.animateItem(),
                                                actionOrElseOrDisabled = ActionOrElse.instant(
                                                    suggest.onClick
                                                ),
                                                titleOrIcon = TitleOrIcon.Title(suggest.comment.text),
                                                importanceToActivate = null,
                                            )
                                        }
                                    }
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
