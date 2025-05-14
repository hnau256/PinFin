@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.EditingString
import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.toEditingString
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.Icon
import hnau.pinfin.model.utils.icons.IconCategory
import hnau.pinfin.model.utils.icons.IconInfo
import hnau.shuffler.annotations.Shuffle
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
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val selected: Icon?,
    val onSelect: (Icon) -> Unit,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies

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

    val icons: StateFlow<Loadable<NonEmptyList<Pair<IconInfo, Boolean>>?>> = combine(
        flow = skeleton.query,
        flow2 = skeleton.selectedCategory,
    ) { queryRaw, selectedCategory ->
        withContext(Dispatchers.Default) {
            val query = queryRaw.text.lowercase().takeIf(String::isNotEmpty)
            IconInfo
                .entries
                .mapNotNull { info ->
                    if (selectedCategory != null && info.category != selectedCategory) {
                        return@mapNotNull null
                    }
                    if (query == null) {
                        return@mapNotNull true to info
                    }
                    val bestSearchIndex = info.tags
                        .mapNotNull { tag ->
                            tag
                                .indexOf(query)
                                .takeIf { it >= 0 }
                        }
                        .minOrNull()
                        ?: return@mapNotNull null
                    val fromStart = bestSearchIndex == 0
                    fromStart to info
                }
                .sortedByDescending { (queryFromStart, info) ->
                    info.popularity - (if (queryFromStart) 0 else Int.MAX_VALUE / 2)
                }
                .map { (_, info) ->
                    info to (info.key == selected)
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

    override val goBackHandler: GoBackHandler = skeleton
        .selectedCategory
        .scopedInState(scope)
        .flatMapState(scope) { (selectionScope, selectedCategories) ->
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
                            .mapState(selectionScope) { query ->
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