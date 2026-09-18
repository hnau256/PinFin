@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.edit

import arrow.core.Option
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.kotlin.coroutines.flow.state.combineState
import org.hnau.commons.kotlin.coroutines.flow.state.combineStateWith
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldBoolean
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

    val input: MutableStateFlow<String>
        get() = skeleton.input

    data class Variant<out T>(
        val value: T,
        val onClickIfNotSelected: (() -> Unit)?,
    )

    val variants: StateFlow<List<Variant<T>>?> = navigateContext
        .isFocused
        .flatMapWithScope(scope) { scope, isFocused ->
            isFocused.foldBoolean(
                ifFalse = { null.toMutableStateFlowAsInitial() },
                ifTrue = { getVariants(scope) },
            )
        }

    private fun getVariants(
        scope: CoroutineScope,
    ): StateFlow<List<Variant<T>>> = combineState(
        scope = scope,
        first = getBaseVariants(scope),
        second = input,
    ) { variants, input ->
        val clearQuery = input.trim()
        val query = clearQuery.normalized

        variants
            .filter { variant ->
                val title = extractTitle(variant).normalized
                title.contains(query)
            }
            .plus(
                createAdditionalVariants(clearQuery)
            )
            .distinctBy { variant ->
                extractKey(variant)
            }
    }.combineStateWith(
        scope = scope,
        other = selected,
    ) { variants, selected ->
        variants.map { variant ->
            Variant(
                value = variant,
                onClickIfNotSelected = selected.fold(
                    ifEmpty = { true },
                    ifSome = { variant != it }
                ).ifTrue {
                    { onSelectedChanged(variant) }
                }
            )
        }
    }

    private val String.normalized: String
        get() = trim().lowercase()

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}