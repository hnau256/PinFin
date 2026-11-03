package org.hnau.pinfin.projector.transaction.edit.utils.chooseorcreate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import arrow.core.NonEmptyList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SLine
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.STextField
import org.hnau.commons.app.projector.fractal.distance.LocalDistance
import org.hnau.commons.app.projector.fractal.padding.ConsumeContentPadding
import org.hnau.commons.app.projector.fractal.padding.LocalContentPadding
import org.hnau.commons.app.projector.fractal.size.units
import org.hnau.commons.app.projector.fractal.table.lazy.cell
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.model.transaction.edit.utils.chooseorcreate.BaseOrAdditional
import org.hnau.pinfin.model.transaction.edit.utils.chooseorcreate.BaseOrAdditionalValues
import org.hnau.pinfin.model.transaction.edit.utils.chooseorcreate.ChooseOrCreateModel
import org.hnau.pinfin.model.transaction.edit.utils.chooseorcreate.fold
import org.hnau.pinfin.model.transaction.edit.utils.chooseorcreate.foldRaw
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.transaction.edit.SChipsRow
import org.hnau.pinfin.projector.transaction.edit.utils.TransactionSection
import org.hnau.pinfin.projector.utils.Chip
import org.hnau.pinfin.projector.utils.ChipInfo
import kotlin.coroutines.EmptyCoroutineContext

class ChooseOrCreateProjector<T>(
    scope: CoroutineScope,
    title: String,
    model: ChooseOrCreateModel<T>,
    dependencies: Dependencies,
    private val messages: ChooseOrCreateMessages,
    extractChipInfo: (T) -> ChipInfo,
) {

    @Pipe
    interface Dependencies {

        fun focused(): State.Focused.Dependencies
    }

    @Fold(
        ordinal = true,
    )
    sealed class State<out T> {

        @Composable
        abstract fun Content()

        data class Variant(
            val info: ChipInfo,
            val selected: Boolean,
            val onClick: ActionOrElse.Action<Unit>,
        ) {

            @Composable
            fun Content() {
                Chip(
                    selected = selected,
                    info = info,
                    actionOrElseOrDisabled = onClick,
                )
            }
        }

        data class Unfocused<out T>(
            private val scope: CoroutineScope,
            private val title: String,
            private val state: ChooseOrCreateModel.State.Unfocused<T>,
            private val extractChipInfo: (T) -> ChipInfo,
            private val messages: ChooseOrCreateMessages,
        ) : State<T>() {

            private val variant: StateFlow<Variant> = state
                .selected
                .mapState(scope) { entityOrNone ->
                    Variant(
                        info = entityOrNone.fold(
                            ifEmpty = { ChipInfo.forNoItem(messages.entityTypeName) },
                            ifSome = extractChipInfo,
                        ),
                        selected = false,
                        onClick = state.onClick,
                    )

                }

            @Composable
            override fun Content() {
                SLine(
                    orientation = Orientation.Vertical,
                    separation = LocalDistance.current.units.padding.along.medium,
                ) {
                    SText(
                        text = title,
                    )
                    variant
                        .collectAsState()
                        .value
                        .Content()
                }
            }
        }

        data class Focused<out T>(
            private val scope: CoroutineScope,
            private val state: ChooseOrCreateModel.State.Focused<T>,
            private val extractChipInfo: (T) -> ChipInfo,
            private val dependencies: Dependencies,
            private val messages: ChooseOrCreateMessages,
        ) : State<T>() {

            @Pipe
            interface Dependencies {

                val localization: Localization
            }

            data class Section(
                val title: String,
                val state: StateFlow<State>,
            ) {

                @Composable
                fun Content() {
                    SLine(
                        orientation = Orientation.Vertical,
                        separation = LocalDistance.current.units.padding.along.small,
                    ) {
                        SText(
                            text = title,
                            maxLines = 1,
                        )
                        state
                            .collectAsState()
                            .value
                            .StateContent(
                                modifier = Modifier.fillMaxWidth(),
                                label = "present_or_empty",
                                contentKey = State::ordinal,
                                transitionSpec = TransitionSpec.crossfade(),
                            ) { state ->
                                state.Content()
                            }
                    }
                }

                @Fold(
                    ordinal = true
                )
                sealed interface State {

                    @Composable
                    fun Content()

                    data class Present(
                        val variants: StateFlow<NonEmptyList<Variant>>,
                    ) : State {

                        @Composable
                        override fun Content() {
                            SChipsRow(
                                items = variants.collectAsState().value,
                            ) { variant ->
                                variant.Content()
                            }
                        }
                    }

                    data class Empty(
                        val message: StateFlow<String>,
                    ) : State {

                        @Composable
                        override fun Content() {
                            Box(
                                modifier = Modifier
                                    .padding(LocalContentPadding.current)
                                    .padding(
                                        vertical = LocalDistance.current.units.padding.along.large,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                ConsumeContentPadding {
                                    SText(
                                        text = message
                                            .collectAsState()
                                            .value,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            private val sections: BaseOrAdditionalValues<Section> = state
                .variants
                .map { baseOrAdditional, value ->
                    val sectionMessages = messages.sections[baseOrAdditional]
                    Section(
                        title = sectionMessages.title,
                        state = value.mapWithScope(scope) { scope, state ->
                            state.fold(
                                ifEmpty = { input ->
                                    Section.State.Empty(
                                        input.mapState(scope) { input ->
                                            input
                                                .takeIf(String::isNotEmpty)
                                                .foldNullable(
                                                    ifNull = { sectionMessages.noVariantsNoInput },
                                                    ifNotNull = sectionMessages.noVariantsWithInput,
                                                )
                                        }
                                    )
                                },
                                ifPresent = { variants ->
                                    Section.State.Present(
                                        variants = variants.mapState(scope) { variants ->
                                            variants.map { variant ->
                                                Variant(
                                                    info = extractChipInfo(variant.value),
                                                    selected = variant.selected,
                                                    onClick = variant.onClick,
                                                )
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    )
                }

            @Composable
            override fun Content() {
                SLine(
                    orientation = Orientation.Vertical,
                    separation = LocalDistance.current.units.padding.along.medium,
                ) {
                    BaseOrAdditional
                        .entries
                        .forEach { baseOrAdditional ->
                            sections[baseOrAdditional].Content()
                        }
                    InputContent()
                }
            }

            @Composable
            private fun InputContent() {
                SItem(
                    topAccessory = { SText(dependencies.localization.searchCreate) },
                ) {
                    STextField(
                        value = state.input.collectAsState(EmptyCoroutineContext).value,
                        onValueChanged = { state.input.value = it },
                        lineLimits = TextFieldLineLimits.SingleLine,
                    )
                }
            }
        }
    }

    private val state: StateFlow<State<T>> = model
        .state
        .mapWithScope(scope) { scope, state ->
            state.foldRaw(
                ifFocused = { state ->
                    State.Focused(
                        scope = scope,
                        state = state,
                        extractChipInfo = extractChipInfo,
                        dependencies = dependencies.focused(),
                        messages = messages,
                    )
                },
                ifUnfocused = { state ->
                    State.Unfocused(
                        scope = scope,
                        title = title,
                        state = state,
                        extractChipInfo = extractChipInfo,
                        messages = messages,
                    )
                }
            )
        }

    val section: TransactionSection<Unit> = TransactionSection.immutable {
        cell {
            SPanel {
                Content()
            }
        }
    }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        state
            .collectAsState()
            .value
            .StateContent(
                modifier = modifier.fillMaxWidth(),
                label = "choose_or_create_state",
                contentKey = State<T>::ordinal,
                transitionSpec = TransitionSpec.remember(
                    showAlignment = Alignment.BottomCenter,
                    hideAlignment = Alignment.BottomCenter,
                )
            ) { state ->
                state.Content()
            }
    }
}