@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.type.utils

import arrow.core.toOption
import hnau.common.model.EditingString
import hnau.common.model.goback.GoBackHandler
import hnau.common.model.goback.GoBackHandlerProvider
import hnau.common.model.goback.NeverGoBackHandler
import hnau.common.model.toEditingString
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.CategoryDirection
import hnau.pinfin.data.CategoryId
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.choose.ChooseState
import hnau.pipe.annotations.Pipe
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

    @Pipe
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
                            direction = direction,
                            title = query,
                        )
                    )
                }
        },
        onReady = onReady,
    )

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}