@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.pageable

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.toOption
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.kotlin.coroutines.flow.state.combineStateWith
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.commons.kotlin.toAccessor
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Comment
import org.hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import org.hnau.pinfin.model.transaction.utils.Editable
import org.hnau.pinfin.model.transaction.utils.allRecords
import org.hnau.pinfin.model.transaction.utils.valueOrNone
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.commons.gen.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Instant

class CategoryModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    val goForward: () -> Unit,
    private val comment: StateFlow<Comment>,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        fun chooseOrCreate(): ChooseOrCreateModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var chooseOrCreate: ChooseOrCreateModel.Skeleton? = null,
        val initialCategory: CategoryInfo?,
        val manualCategory: MutableStateFlow<CategoryInfo?> =
            initialCategory.toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                initialCategory = null,
            )

            fun createForEdit(
                category: CategoryInfo,
            ): Skeleton = Skeleton(
                initialCategory = category,
            )
        }
    }

    fun createPage(
        scope: CoroutineScope,
        usedCategories: StateFlow<Set<CategoryInfo>>,
    ): ChooseOrCreateModel<CategoryInfo> = ChooseOrCreateModel(
        scope = scope,
        dependencies = dependencies.chooseOrCreate(),
        skeleton = skeleton::chooseOrCreate
            .toAccessor()
            .getOrInit { ChooseOrCreateModel.Skeleton() },
        extractItemsFromState = BudgetState::categories,
        additionalItems = usedCategories,
        itemTextMapper = Mapper(
            direct = CategoryInfo::title,
            reverse = { title ->
                CategoryInfo(
                    id = CategoryId(title),
                    config = null,
                )
            }
        ),
        selected = categoryEditable.mapState(scope, Editable<CategoryInfo>::valueOrNone),
        onReady = { selected ->
            skeleton.manualCategory.value = selected
            goForward()
        }
    )

    private fun getCategoryBasedOnComment(
        scope: CoroutineScope,
    ): StateFlow<Option<CategoryInfo>> = dependencies
        .budgetRepository
        .state
        .combineStateWith(
            scope = scope,
            other = comment,
        ) { state, comment ->
            state to comment
        }
        .mapLatest { (state, commentRaw) ->
            withContext(Dispatchers.Default) {
                commentRaw
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
                            .maxByOrNull(Pair<Instant, *>::first)
                            ?.second
                    }
                    .toOption()
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = None,
        )

    internal val categoryEditable: StateFlow<Editable<CategoryInfo>> = Editable.create(
        scope = scope,
        valueOrNone = skeleton
            .manualCategory
            .flatMapWithScope(scope) { scope, manualOrNull ->
                manualOrNull
                    .toOption()
                    .fold(
                        ifSome = { Some(it).toMutableStateFlowAsInitial() },
                        ifEmpty = { getCategoryBasedOnComment(scope) },
                    )
            },
        initialValueOrNone = skeleton.initialCategory.toOption(),
    )

    val category: StateFlow<CategoryInfo?> = categoryEditable
        .mapState(scope) { it.valueOrNone.getOrNull() }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}