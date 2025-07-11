@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.type.entry.record

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import arrow.core.toNonEmptyListOrNone
import arrow.core.toNonEmptyListOrNull
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.model.EditingString
import hnau.common.model.goback.GoBackHandler
import hnau.common.model.goback.GoBackHandlerProvider
import hnau.common.model.goback.NeverGoBackHandler
import hnau.common.model.toEditingString
import hnau.pinfin.data.Comment
import hnau.pinfin.data.Record
import hnau.pinfin.model.AmountModel
import hnau.pinfin.model.transaction.type.utils.ChooseCategoryModel
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Duration.Companion.days

class RecordModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val remove: StateFlow<(() -> Unit)?>,
    localUsedCategories: StateFlow<Set<CategoryInfo>>,
    val createNextIfLast: StateFlow<(() -> Unit)?>,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val category: MutableStateFlow<CategoryInfo?>,
        val amount: AmountModel.Skeleton,
        val comment: MutableStateFlow<EditingString>,
        val overlapDialog: MutableStateFlow<OverlapDialog?> = null.toMutableStateFlowAsInitial(),
    ) {

        constructor(
            record: TransactionInfo.Type.Entry.Record,
        ) : this(
            category = record.category.toMutableStateFlowAsInitial(),
            amount = AmountModel.Skeleton(
                amount = record.amount,
            ),
            comment = record
                .comment
                .text
                .toEditingString()
                .toMutableStateFlowAsInitial(),
        )

        @Serializable
        sealed interface OverlapDialog {

            @Serializable
            @SerialName("remove")
            data object Remove : OverlapDialog

            @Serializable
            @SerialName("choose_category")
            data class ChooseCategory(
                val chooseCategorySkeleton: ChooseCategoryModel.Skeleton,
            ) : OverlapDialog
        }

        companion object {

            val empty: Skeleton
                get() = Skeleton(
                    category = null.toMutableStateFlowAsInitial(),
                    amount = AmountModel.Skeleton.empty,
                    comment = "".toEditingString().toMutableStateFlowAsInitial(),
                )
        }
    }

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        fun amount(): AmountModel.Dependencies

        fun chooseCategory(): ChooseCategoryModel.Dependencies
    }

    val category: StateFlow<CategoryInfo?>
        get() = skeleton.category

    fun openCategoryChooser() {
        skeleton.overlapDialog.value = Skeleton.OverlapDialog.ChooseCategory(
            chooseCategorySkeleton = ChooseCategoryModel.Skeleton.empty
        )
    }

    val openRemoveOverlap: StateFlow<(() -> Unit)?> = remove.mapState(
        scope = scope,
    ) { removeOrNull ->
        removeOrNull?.let {
            { skeleton.overlapDialog.value = Skeleton.OverlapDialog.Remove }
        }
    }

    val amount = AmountModel(
        scope = scope,
        dependencies = dependencies.amount(),
        skeleton = skeleton.amount,
    )

    sealed interface OverlapDialogModel {

        data class Remove(
            val remove: () -> Unit,
        ) : OverlapDialogModel

        data class ChooseCategory(
            val chooseCategoryModel: ChooseCategoryModel,
        ) : OverlapDialogModel
    }

    fun closeOverlap() {
        skeleton.overlapDialog.value = null
    }

    val overlap: StateFlow<OverlapDialogModel?> = skeleton
        .overlapDialog
        .scopedInState(
            parentScope = scope,
        )
        .flatMapState(
            scope = scope,
        ) { (stateScope, dialogOrNull) ->
            when (dialogOrNull) {

                null -> null.toMutableStateFlowAsInitial()

                is Skeleton.OverlapDialog.ChooseCategory -> OverlapDialogModel.ChooseCategory(
                    ChooseCategoryModel(
                        scope = stateScope,
                        dependencies = dependencies.chooseCategory(),
                        skeleton = dialogOrNull.chooseCategorySkeleton,
                        selected = skeleton.category,
                        updateSelected = { skeleton.category.value = it },
                        onReady = ::closeOverlap,
                        localUsedCategories = localUsedCategories,
                    )
                ).toMutableStateFlowAsInitial()

                Skeleton.OverlapDialog.Remove -> remove.mapState(
                    scope = stateScope,
                ) { removeOrNull ->
                    removeOrNull?.let { remove ->
                        OverlapDialogModel.Remove(
                            remove = remove,
                        )
                    }
                }
            }
        }

    val comment: MutableStateFlow<EditingString>
        get() = skeleton.comment

    private data class CommentSuggest(
        val comment: Comment,
        val normalized: String,
        val timestamp: Instant,
        val equalsFromBeginning: Boolean,
    )

    val commentSuggests: StateFlow<NonEmptyList<Comment>?> = dependencies
        .budgetRepository
        .state
        .combine(
            flow = comment,
        ) { state, commentEditingString ->
            withContext(Dispatchers.Default) {
                val queryRaw = commentEditingString
                    .text
                    .trim()
                val query = queryRaw
                    .lowercase()
                    .takeIf { it.isNotEmpty() }
                    ?: return@withContext null
                state
                    .transactions
                    .flatMap { transaction ->
                        when (val type = transaction.type) {
                            is TransactionInfo.Type.Entry -> type
                                .records
                                .map { record ->
                                    record.comment to transaction.timestamp
                                }

                            is TransactionInfo.Type.Transfer -> emptyList()
                        }
                    }
                    .mapNotNull { (comment, timestamp) ->
                        val trimmed = comment
                            .text
                            .trim()
                        if (trimmed == queryRaw) {
                            return@mapNotNull null
                        }
                        val normalized = trimmed.lowercase()
                        val equalsIndex = normalized
                            .indexOf(
                                string = query,
                                ignoreCase = true,
                            )
                            .takeIf { it >= 0 }
                            ?: return@mapNotNull null
                        CommentSuggest(
                            comment = comment,
                            timestamp = timestamp,
                            equalsFromBeginning = equalsIndex == 0,
                            normalized = normalized,
                        )
                    }
                    .sortedByDescending { suggest ->
                        val equalsFromBeginningWeight = suggest.equalsFromBeginning.foldBoolean(
                            ifTrue = { 1000.days },
                            ifFalse = { 0.days }
                        )
                        suggest.timestamp.epochSeconds + equalsFromBeginningWeight.inWholeSeconds
                    }
                    .distinctBy(CommentSuggest::normalized)
                    .take(16)
                    .map(CommentSuggest::comment)
                    .toNonEmptyListOrNull()
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    val record: StateFlow<Record?> = skeleton
        .category
        .combineStateWith(
            scope = scope,
            other = amount.amount,
        ) { categoryOrNull, amountOrNull ->
            categoryOrNull?.let { category ->
                amountOrNull?.let { amount ->
                    category to amount
                }
            }
        }
        .combineStateWith(
            scope = scope,
            other = skeleton.comment,
        ) { categoryAndAmountOrNull, comment ->
            val (category, amount) = categoryAndAmountOrNull ?: return@combineStateWith null
            Record(
                category = category.id,
                amount = amount,
                comment = comment.text.let(::Comment),
            )
        }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}