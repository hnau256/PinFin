@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.client.model.transaction.type.utils

import arrow.core.toOption
import hnau.common.app.EditingString
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.toEditingString
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.client.data.budget.BudgetRepository
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
    localUsedCategories: StateFlow<Set<CategoryId>>,
    dependencies: Dependencies,
    selected: StateFlow<CategoryId?>,
    updateSelected: (CategoryId) -> Unit,
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

    private val categories: StateFlow<Loadable<List<CategoryId>>> = dependencies
        .repository
        .category
        .list
        .combineStateWith(
            scope = scope,
            other = localUsedCategories,
        ) { accountsOrLoading, localUsedCategories ->
            accountsOrLoading.map { accounts ->
                accounts
                    .toSet()
                    .plus(localUsedCategories)
                    .sorted()
            }
        }

    val state: ChooseState<CategoryId> = ChooseState(
        scope = scope,
        variants = categories,
        selected = selected.mapState(scope) { it.toOption() },
        updateSelected = updateSelected,
        query = skeleton.query,
        extractId = { id },
        extractAdditionalFields = {
            //TODO title
            emptyList()
        },
        createPossibleNewVariantsByQuery = { query ->
            CategoryDirection
                .entries
                .map { direction ->
                    CategoryId(
                        CategoryId.directionPrefixes[direction] + query
                    )
                }
        },
        onReady = onReady,
    )
}