@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.client.model.transaction.type.utils

import arrow.core.toOption
import hnau.common.app.EditingString
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.toEditingString
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.client.data.budget.BudgetRepository
import hnau.pinfin.client.data.budget.CategoryInfo
import hnau.pinfin.client.model.utils.choose.ChooseState
import hnau.pinfin.scheme.CategoryDirection
import hnau.pinfin.scheme.CategoryId
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class ChooseCategoryModel(
    scope: CoroutineScope,
    skeleton: Skeleton,
    localUsedCategories: StateFlow<Set<CategoryInfo>>,
    dependencies: Dependencies,
    selected: StateFlow<CategoryInfo?>,
    updateSelected: (CategoryInfo) -> Unit,
    onReady: () -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val query: MutableStateFlow<EditingString> =
            "".toEditingString().toMutableStateFlowAsInitial(),
    ) {

        companion object {

            val empty: Skeleton
                get() = Skeleton(
                    query = "".toEditingString().toMutableStateFlowAsInitial(),
                )
        }
    }

    @Shuffle
    interface Dependencies {

        val repository: BudgetRepository
    }

    private val categories: StateFlow<List<CategoryInfo>> = dependencies
        .repository
        .categories
        .list
        .combineStateWith(
            scope = scope,
            other = localUsedCategories,
        ) { accounts, localUsedCategories ->
            (accounts + localUsedCategories).distinct().sorted()
        }

    val state = ChooseState(
        scope = scope,
        variants = categories,
        selected = selected.mapState(scope) { it.toOption() },
        updateSelected = updateSelected,
        query = skeleton.query,
        extractId = { it.id.id },
        extractAdditionalFields = { info ->
            listOf(
                info.title
            )
        },
        createPossibleNewVariantsByQuery = { query ->
            CategoryDirection
                .entries
                .map { direction ->
                    CategoryInfo(
                        id = CategoryId(
                            CategoryId.directionPrefixes[direction] + query
                        )
                    )
                }
        },
        onReady = onReady,
    )
}