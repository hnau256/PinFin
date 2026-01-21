@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.model.EditingString
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.toEditingString
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.coroutines.flow.state.flatMapWithScope
import hnau.common.kotlin.coroutines.flow.state.mapState
import hnau.common.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.utils.icons.IconCategory
import hnau.pinfin.model.utils.icons.IconVariant
import hnau.pinfin.model.utils.icons.category
import hnau.pinfin.model.utils.icons.tags
import hnau.pinfin.model.utils.icons.weight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class IconModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val selected: IconVariant?,
    val onSelect: (IconVariant) -> Unit,
) {


    @Serializable
    data class Skeleton(
        val selectedCategory: MutableStateFlow<IconCategory?> =
            null.toMutableStateFlowAsInitial(),
        val query: MutableStateFlow<EditingString> =
            "".toEditingString().toMutableStateFlowAsInitial(),
    )

    val query: MutableStateFlow<EditingString>
        get() = skeleton.query

    val selectedCategory: StateFlow<IconCategory?>
        get() = skeleton.selectedCategory

    fun onCategoryClick(
        category: IconCategory,
    ) {
        skeleton.selectedCategory.update { selectedCategory ->
            category.takeIf { it != selectedCategory }
        }
    }

    val icons: StateFlow<Loadable<NonEmptyList<Pair<IconVariant, Boolean>>?>> = combine(
        flow = skeleton.query,
        flow2 = skeleton.selectedCategory,
    ) { queryRaw, selectedCategory ->
        withContext(Dispatchers.Default) {
            val query = queryRaw.text.lowercase().takeIf(String::isNotEmpty)
            IconVariant
                .entries
                .mapNotNull { variant ->
                    if (selectedCategory != null && variant.category != selectedCategory) {
                        return@mapNotNull null
                    }
                    if (query == null) {
                        return@mapNotNull true to variant
                    }
                    val bestSearchIndex = variant.tags
                        .mapNotNull { tag ->
                            tag
                                .indexOf(query)
                                .takeIf { it >= 0 }
                        }
                        .minOrNull()
                        ?: return@mapNotNull null
                    val fromStart = bestSearchIndex == 0
                    fromStart to variant
                }
                .sortedByDescending { (queryFromStart, info) ->
                    info.weight - (if (queryFromStart) 0 else Int.MAX_VALUE / 2)
                }
                .map { (_, variant) ->
                    variant to (variant == selected)
                }
                .toNonEmptyListOrNull()
                .let(::Ready)
        }
    }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = Loading,
        )

    val goBackHandler: GoBackHandler = skeleton
        .selectedCategory
        .flatMapWithScope(scope) { scope, selectedCategories ->
            selectedCategories
                .foldNullable(
                    ifNotNull = {
                        {
                            skeleton.selectedCategory.value = null
                        }.toMutableStateFlowAsInitial()
                    },
                    ifNull = {
                        skeleton
                            .query
                            .mapState(scope) { query ->
                                query
                                    .text
                                    .isNotEmpty()
                                    .foldBoolean(
                                        ifTrue = {
                                            {
                                                skeleton.query.value = "".toEditingString()
                                            }
                                        },
                                        ifFalse = { null },
                                    )
                            }
                    }
                )
        }
}