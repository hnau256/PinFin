@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.filter.pageable

import arrow.core.NonEmptyList
import arrow.core.NonEmptySet
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.flow.state.combineStateWith
import hnau.common.kotlin.coroutines.flow.state.flatMapWithScope
import hnau.common.kotlin.coroutines.flow.state.mapState
import hnau.common.kotlin.coroutines.flow.state.mapWithScope
import hnau.common.kotlin.coroutines.flow.state.mutable.mapMutableState
import hnau.common.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.ifTrue
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.CategoryId
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class SelectCategoriesModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val selectedCategories: MutableStateFlow<Set<CategoryId?>>,
    ) {

        companion object {

            fun create(
                initialSelectedCategoriesIds: NonEmptySet<CategoryId?>?,
            ): Skeleton = Skeleton(
                selectedCategories = initialSelectedCategoriesIds
                    .ifNull { emptyList() }
                    .toSet()
                    .toMutableStateFlowAsInitial(),
            )
        }
    }

    class Page(
        val categories: StateFlow<List<Category>>,
    ) {

        data class Category(
            val info: CategoryInfo,
            val selected: MutableStateFlow<Boolean>,
        )

        val goBackHandler: GoBackHandler
            get() = NeverGoBackHandler
    }

    private val categories: StateFlow<List<Page.Category>> = dependencies
        .budgetRepository
        .state
        .mapWithScope(scope) { scope, state ->
            state
                .categories
                .map { info ->
                    val id = info.id

                    val updateIds: (Set<CategoryId?>, Boolean) -> Set<CategoryId?> =
                        { selectedIds, selected ->
                            selected.foldBoolean(
                                ifTrue = { selectedIds + id },
                                ifFalse = { selectedIds - id }
                            )
                        }

                    Page.Category(
                        info = info,
                        selected = skeleton
                            .selectedCategories
                            .mapMutableState(
                                scope = scope,
                                transform = { selectedIds -> id in selectedIds },
                                set = { selected ->
                                    update { selectedIds ->
                                        updateIds(selectedIds, selected)
                                    }
                                },
                                compareAndSet = { expectSelected, selected ->
                                    val current = value
                                    compareAndSet(
                                        updateIds(current, expectSelected),
                                        updateIds(current, selected),
                                    )
                                },
                            )
                    )
                }
        }

    val selectedCategories: StateFlow<NonEmptyList<CategoryInfo>?> = categories
        .mapWithScope(scope) { scope, categories ->
            categories.map { category ->
                category
                    .selected
                    .mapState(scope) { selected ->
                        selected.ifTrue { category.info }
                    }
            }
        }
        .flatMapWithScope(
            scope = scope,
        ) { scope, categories ->
            categories
                .drop(1)
                .fold(
                    initial = categories
                        .firstOrNull()
                        .foldNullable(
                            ifNull = { emptySet<CategoryInfo>().toMutableStateFlowAsInitial() },
                            ifNotNull = { first ->
                                first.mapState(scope) { setOfNotNull(it) }
                            }
                        ),
                ) { acc, categoryOrNull ->
                    acc
                        .combineStateWith(
                            scope = scope,
                            other = categoryOrNull,
                        ) { acc, categoryOrNull ->
                            categoryOrNull.foldNullable(
                                ifNull = { acc },
                                ifNotNull = { acc + it }
                            )
                        }
                }
                .mapState(scope) { categories ->
                    categories
                        .toList()
                        .sorted()
                        .toNonEmptyListOrNull()
                }
        }

    val selectedCategoriesIds: StateFlow<NonEmptySet<CategoryId?>?> = selectedCategories
        .mapState(scope) { categories ->
            categories
                ?.map { category -> category.id }
                ?.toNonEmptySet()
        }

    fun clear() {
        skeleton.selectedCategories.value = emptySet()
    }

    fun createPage(): Page = Page(
        categories = categories,
    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}