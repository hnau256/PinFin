@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.edit.utils.chooseorcreate

import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.toNonEmptyListOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.flow.state.Stickable
import org.hnau.commons.kotlin.coroutines.flow.state.combineStateWith
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.coroutines.flow.state.stateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.stick
import org.hnau.commons.kotlin.coroutines.instant
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.ifTrue
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.transaction.edit.utils.EditNavigateContext

class ChooseOrCreateModel<T>(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val getBaseVariants: (CoroutineScope) -> StateFlow<List<T>>,
    private val selected: StateFlow<Option<T>>,
    private val onSelectedChanged: (T) -> Unit,
    private val createAdditionalVariants: (query: String) -> List<T>,
    private val extractKey: (T) -> Any?,
    private val extractTitle: (T) -> String,
    val navigateContext: EditNavigateContext,
) {

    @Serializable
    data class Skeleton(
        val input: MutableStateFlow<String> = "".toMutableStateFlowAsInitial(),
    )

    sealed interface State<out T> {

        data class Unfocused<out T>(
            val selected: StateFlow<Option<T>>,
            val onClick: ActionOrElse.Action<Unit>,
        ) : State<T>

        data class Focused<out T>(
            val input: MutableStateFlow<String>,
            val variants: BaseOrAdditionalValues<StateFlow<VariantsState<T>>>,
        ) : State<T> {

            @Fold(
                ordinal = true,
            )
            sealed interface VariantsState<out T> {

                data class Present<out T>(
                    val variants: StateFlow<NonEmptyList<Variant<T>>>,
                ) : VariantsState<T>

                data class Empty(
                    val input: StateFlow<String>,
                ) : VariantsState<Nothing>
            }

            data class Variant<out T>(
                val value: T,
                val onClick: ActionOrElse.Action<Unit>?,
            )
        }
    }

    @Fold
    sealed interface FocusedVariantsState<out T> {

        data class Present<out T>(
            val variants: NonEmptyList<T>,
        ) : FocusedVariantsState<T>

        data class Empty(
            val input: String,
        ) : FocusedVariantsState<Nothing>
    }

    val state: StateFlow<State<T>> = navigateContext
        .isFocused
        .mapWithScope(
            scope = scope,
        ) { scope, isFocused ->
            isFocused.foldBoolean(
                ifFalse = {
                    State.Unfocused(
                        selected = selected,
                        onClick = ActionOrElse.instant(navigateContext.requestFocus),
                    )
                },
                ifTrue = {

                    val trimmedInput = skeleton
                        .input
                        .mapState(
                            scope = scope,
                            transform = String::trim,
                        )

                    val allBaseVariants = getBaseVariants(scope)

                    State.Focused(
                        input = skeleton.input,
                        variants = BaseOrAdditionalValues(
                            base = allBaseVariants
                                .combineStateWith(
                                    scope = scope,
                                    other = trimmedInput,
                                ) { variants, input ->
                                    input
                                        .normalized
                                        .takeIf(String::isNotEmpty)
                                        .foldNullable(
                                            ifNull = { variants },
                                            ifNotNull = { normalizedInput ->
                                                variants
                                                    .filter { variant ->
                                                        val title = extractTitle(variant).normalized
                                                        title.contains(normalizedInput)
                                                    }
                                            }
                                        )
                                        .toNonEmptyListOrNull()
                                        .foldNullable(
                                            ifNull = {
                                                FocusedVariantsState.Empty(
                                                    input = input,
                                                )
                                            },
                                            ifNotNull = { variants ->
                                                FocusedVariantsState.Present(
                                                    variants = variants,
                                                )
                                            },
                                        )
                                },
                            additional = trimmedInput
                                .flatMapWithScope(scope) { scope, trimmedInput ->
                                    trimmedInput
                                        .takeIf(String::isNotEmpty)
                                        .foldNullable(
                                            ifNull = {
                                                FocusedVariantsState
                                                    .Empty("")
                                                    .toMutableStateFlowAsInitial()
                                            },
                                            ifNotNull = { input ->
                                                allBaseVariants.mapState(scope) { baseVariants ->
                                                    val baseKeys = baseVariants.map(extractKey)
                                                    input
                                                        .let(createAdditionalVariants)
                                                        .filter { additionalVariant ->
                                                            val key = extractKey(additionalVariant)
                                                            key !in baseKeys
                                                        }
                                                        .toNonEmptyListOrNull()
                                                        .foldNullable(
                                                            ifNull = {
                                                                FocusedVariantsState.Empty(
                                                                    input = input,
                                                                )
                                                            },
                                                            ifNotNull = { variants ->
                                                                FocusedVariantsState.Present(
                                                                    variants = variants,
                                                                )
                                                            },
                                                        )
                                                }
                                            }
                                        )

                                }
                        ).map { variants ->
                            variants.stick(scope)
                        },
                    )
                }
            )
        }

    private fun StateFlow<FocusedVariantsState<T>>.stick(
        scope: CoroutineScope,
    ): StateFlow<State.Focused.VariantsState<T>> = stick(
        scope = scope,
    ) { scope, initial ->
        initial.fold(
            ifEmpty = { initialInput ->
                Stickable.stateFlow(
                    initial = initialInput,
                    tryUseNext = { state ->
                        state.fold(
                            ifEmpty = ::Some,
                            ifPresent = { None },
                        )
                    },
                    createResult = { inputState ->
                        State.Focused.VariantsState.Empty(inputState)
                    }
                )
            },
            ifPresent = { initialVariants ->

                Stickable.stateFlow(
                    initial = initialVariants,
                    tryUseNext = { state ->
                        state.fold(
                            ifPresent = ::Some,
                            ifEmpty = { None },
                        )
                    },
                    createResult = { variantsState ->
                        variantsState
                            .combineStateWith(
                                scope = scope,
                                other = selected.mapState(scope) { it.map(extractKey) },
                            ) { variants, selectedKey ->
                                variants.map { variant ->
                                    State.Focused.Variant(
                                        value = variant,
                                        onClick = selectedKey
                                            .fold(
                                                ifEmpty = { true },
                                                ifSome = { variant.let(extractKey) != it }
                                            )
                                            .ifTrue {
                                                ActionOrElse.instant {
                                                    onSelectedChanged(variant)
                                                    navigateContext.goForward()
                                                }
                                            },
                                    )
                                }
                            }
                            .let { variants ->
                                State.Focused.VariantsState.Present(variants)
                            }

                    }
                )
            }
        )
    }

    private val String.normalized: String
        get() = trim().lowercase()

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}