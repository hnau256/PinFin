package org.hnau.pinfin.model.transaction.edit

import arrow.core.toOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.combineStateWith
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Comment
import org.hnau.pinfin.model.transaction.edit.utils.EditNavigateContext
import org.hnau.pinfin.model.transaction.utils.allRecords
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

class CategoryChooseModel(
    delegate: Delegate,
    navigateContext: EditNavigateContext,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val initialIdWithCategory: KeyValue<CategoryId, CategoryInfo>?,
        val choose: ChooseOrCreateModel.Skeleton = ChooseOrCreateModel.Skeleton(),
        val manualIdWithCategory: MutableStateFlow<KeyValue<CategoryId, CategoryInfo>?> = initialIdWithCategory.toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                initialIdWithCategory = null,
            )

            fun create(
                idWithCategory: KeyValue<CategoryId, CategoryInfo>,
            ): Skeleton = Skeleton(
                initialIdWithCategory = idWithCategory,
            )
        }
    }

    class Delegate(
        val scope: CoroutineScope,
        val skeleton: Skeleton,
        val dependencies: Dependencies,
        comment: StateFlow<Comment>,
    ) {

        private val suggestedCategory: StateFlow<KeyValue<CategoryId, CategoryInfo>?> = dependencies
            .budgetRepository
            .state
            .combineStateWith(
                scope = scope,
                other = comment,
            ) { state, comment -> state to comment }
            .mapLatest { (state, comment) ->
                withContext(Dispatchers.Default) {
                    comment
                        .text
                        .trim()
                        .takeIf(String::isNotEmpty)
                        ?.let { comment ->
                            state
                                .allRecords
                                .mapNotNull { (timestamp, record) ->
                                    record
                                        .takeIf {
                                            it.comment.text.trim().equals(
                                                other = comment,
                                                ignoreCase = true,
                                            )
                                        }
                                        ?.let { recordWithSameComment ->
                                            timestamp to recordWithSameComment.category
                                        }
                                }
                                .maxByOrNull(Pair<LocalDate, *>::first)
                                ?.second
                        }
                }
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )

        val actualCategory: StateFlow<KeyValue<CategoryId, CategoryInfo>?> = skeleton
            .manualIdWithCategory
            .flatMapWithScope(scope) { scope, manualCategoryOrNull ->
                manualCategoryOrNull
                    .foldNullable(
                        ifNotNull = { it.toMutableStateFlowAsInitial() },
                        ifNull = { suggestedCategory },
                    )
            }
    }

    val choose: ChooseOrCreateModel<KeyValue<CategoryId, CategoryInfo>> = ChooseOrCreateModel(
        scope = delegate.scope,
        skeleton = delegate.skeleton.choose,
        getBaseVariants = { scope ->
            delegate
                .dependencies
                .budgetRepository
                .state
                .mapState(scope) { state ->
                    state.categories
                }
        },
        selected = delegate.actualCategory.mapState(delegate.scope) { it.toOption() },
        onSelectedChanged = delegate.skeleton.manualIdWithCategory::value::set,
        createAdditionalVariants = { query ->
            AmountDirection.entries.map { direction ->
                val id = CategoryId(
                    direction = direction,
                    idSuffix = query,
                )
                KeyValue(
                    key = id,
                    value = CategoryInfo.create(
                        id = id,
                        config = null,
                    )
                )
            }
        },
        extractKey = { it.key },
        extractTitle = { it.value.title },
        navigateContext = navigateContext,
    )

    val categoryEditable: StateFlow<Editable<KeyValue<CategoryId, CategoryInfo>>> =
        Editable.create(
            scope = delegate.scope,
            valueOrNone = delegate.actualCategory.mapState(delegate.scope) { it.toOption() },
            initialValueOrNone = delegate.skeleton.initialIdWithCategory.toOption(),
        )

    val goBackHandler: GoBackHandler
        get() = choose.goBackHandler
}
