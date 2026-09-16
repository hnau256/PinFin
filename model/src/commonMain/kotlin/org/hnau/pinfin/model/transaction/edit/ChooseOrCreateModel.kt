package org.hnau.pinfin.model.transaction.edit

import arrow.core.Option
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.kotlin.coroutines.flow.state.combineState
import org.hnau.commons.kotlin.coroutines.flow.state.combineStateWith
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.ifTrue

class ChooseOrCreateModel<T>(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    variants: StateFlow<List<T>>,
    selected: StateFlow<Option<T>>,
    onSelectedChanged: (T) -> Unit,
    createAdditionalVariants: (query: String) -> List<T>,
    extractKey: (T) -> Any?,
    extractTitle: (T) -> String,
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

    val variants: StateFlow<List<Variant<T>>> = combineState(
        scope = scope,
        first = variants,
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