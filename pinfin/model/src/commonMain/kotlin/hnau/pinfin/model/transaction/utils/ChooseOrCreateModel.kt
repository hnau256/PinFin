@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.utils

import arrow.core.NonEmptyList
import arrow.core.Option
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.model.EditingString
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.toEditingString
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class ChooseOrCreateModel<T : Comparable<T>>(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    extractItemsFromState: (BudgetState) -> List<T>,
    additionalItems: StateFlow<Iterable<T>>,
    private val itemTextMapper: Mapper<T, String>,
    private val selected: StateFlow<Option<T>>,
    onReady: (T) -> Unit,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val query: MutableStateFlow<EditingString> =
            "".toEditingString().toMutableStateFlowAsInitial(),
    )

    val query: MutableStateFlow<EditingString>
        get() = skeleton.query

    data class State<T>(
        val filtered: NonEmptyList<Item<T>>?,
        val new: Item<T>?,
    ) {

        data class Item<T>(
            val value: T,
            val isSelected: StateFlow<Boolean>,
            val onClick: () -> Unit,
        )
    }

    private val trimmedQuery: StateFlow<String?> = skeleton
        .query
        .mapState(scope) { query ->
            query
                .text
                .trim()
                .takeIf(String::isNotEmpty)
        }

    val state: StateFlow<State<T>> = dependencies
        .budgetRepository
        .state
        .mapState(scope, extractItemsFromState)
        .combineStateWith(
            scope = scope,
            other = additionalItems,
        ) { stateItems, additionalItems ->
            (stateItems + additionalItems)
                .distinct()
                .sorted()
        }
        .scopedInState(scope)
        .combineStateWith(
            scope = scope,
            other = trimmedQuery,
        ) { (itemsScope, items), queryOrNull ->

            val createItem: (T) -> State.Item<T> = { item ->
                State.Item(
                    value = item,
                    isSelected = selected
                        .mapState(itemsScope) { selectedItem ->
                            selectedItem
                                .fold(
                                    ifEmpty = { false },
                                    ifSome = { selectedItem -> item == selectedItem }
                                )
                        },
                    onClick = { onReady(item) },
                )
            }

            queryOrNull.foldNullable(
                ifNull = {
                    State(
                        filtered = items
                            .map(createItem)
                            .toNonEmptyListOrNull(),
                        new = null,
                    )
                },
                ifNotNull = { query ->
                    val (filtered, hasAbsolutelySameAsQuery) = items.fold(
                        initial = emptyList<State.Item<T>>() to false
                    ) { (filtered, alreadyHasAbsolutelySameAsQuery), item ->
                        val itemText = itemTextMapper.direct(item).trim()
                        when {
                            itemText == query -> (filtered + createItem(item)) to true
                            itemText.contains(
                                other = query,
                                ignoreCase = true,
                            ) -> (filtered + createItem(item)) to alreadyHasAbsolutelySameAsQuery

                            else -> filtered to alreadyHasAbsolutelySameAsQuery
                        }
                    }
                    State(
                        filtered = filtered.toNonEmptyListOrNull(),
                        new = hasAbsolutelySameAsQuery.foldBoolean(
                            ifTrue = { null },
                            ifFalse = {
                                val item = itemTextMapper.reverse(query)
                                State.Item(
                                    value = item,
                                    isSelected = false.toMutableStateFlowAsInitial(),
                                    onClick = { onReady(item) },
                                )
                            }
                        )
                    )
                }
            )
        }

    val goBackHandler: GoBackHandler = trimmedQuery.mapState(scope) { queryOrNull ->
        queryOrNull?.let { { skeleton.query.value = "".toEditingString() } }
    }
}