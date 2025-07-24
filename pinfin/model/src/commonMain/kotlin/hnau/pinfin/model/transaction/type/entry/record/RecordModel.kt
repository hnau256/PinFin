@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.type.entry.record

import arrow.core.NonEmptyList
import arrow.core.identity
import arrow.core.toNonEmptyListOrNull
import arrow.core.toOption
import hnau.common.app.model.EditingString
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.app.model.toEditingString
import hnau.common.kotlin.coroutines.Stickable
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapStateLite
import hnau.common.kotlin.coroutines.predeterminated
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.stateFlow
import hnau.common.kotlin.coroutines.stick
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

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
        val manualCategory: MutableStateFlow<CategoryInfo?>,
        val amount: AmountModel.Skeleton,
        val comment: MutableStateFlow<EditingString>,
        val overlapDialog: MutableStateFlow<OverlapDialog?> = null.toMutableStateFlowAsInitial(),
    ) {

        constructor(
            record: TransactionInfo.Type.Entry.Record,
        ) : this(
            manualCategory = record.category.toMutableStateFlowAsInitial(),
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
                    manualCategory = null.toMutableStateFlowAsInitial(),
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
                        selected = skeleton.manualCategory,
                        updateSelected = { skeleton.manualCategory.value = it },
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

    private data class CommentInfo(
        val comment: Comment,
        val timestamp: Instant,
        val category: CategoryInfo,
    )

    private data class CommentSuggest(
        val info: CommentInfo,
        val normalized: String,
        val suitableType: SuitableType,
    ) {

        enum class SuitableType {
            Absolute,
            IgnoreCase,
            FromBeginning,
            Other,
            ;
        }
    }

    private val commentSuggestsWithCalculatedCategory: StateFlow<Pair<List<Comment>, CategoryInfo?>> =
        dependencies
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
                        ?: return@withContext emptyList<Comment>() to null

                    val allSuggests = state
                        .transactions
                        .flatMap { transaction ->
                            when (val type = transaction.type) {
                                is TransactionInfo.Type.Entry -> type
                                    .records
                                    .toList()
                                    .flatMap { record ->
                                        record
                                            .comment
                                            .text
                                            .split(',')
                                            .map { comment ->
                                                comment
                                                    .trim()
                                                    .replaceFirstChar(Char::uppercaseChar)
                                            }
                                            .filter(String::isNotEmpty)
                                            .map { comment ->
                                                CommentInfo(
                                                    comment = Comment(comment),
                                                    timestamp = transaction.timestamp,
                                                    category = record.category,
                                                )
                                            }
                                    }

                                is TransactionInfo.Type.Transfer -> emptyList()
                            }
                        }
                        .mapNotNull { info ->
                            val trimmed = info
                                .comment
                                .text
                                .trim()
                            val normalized = trimmed.lowercase()
                            val equalsIndex = normalized
                                .indexOf(
                                    string = query,
                                    ignoreCase = true,
                                )
                                .takeIf { it >= 0 }
                                ?: return@mapNotNull null
                            CommentSuggest(
                                info = info,
                                normalized = normalized,
                                suitableType = when {
                                    trimmed == queryRaw ->
                                        CommentSuggest.SuitableType.Absolute

                                    normalized == query ->
                                        CommentSuggest.SuitableType.IgnoreCase

                                    equalsIndex == 0 ->
                                        CommentSuggest.SuitableType.FromBeginning

                                    else ->
                                        CommentSuggest.SuitableType.Other
                                },
                            )
                        }
                        .sortedByDescending { suggest ->
                            val equalsFromBeginning = when (suggest.suitableType) {
                                CommentSuggest.SuitableType.Absolute,
                                CommentSuggest.SuitableType.IgnoreCase,
                                CommentSuggest.SuitableType.FromBeginning,
                                    -> true

                                CommentSuggest.SuitableType.Other -> false
                            }
                            val equalsFromBeginningWeight = equalsFromBeginning.foldBoolean(
                                ifTrue = { 1000.days },
                                ifFalse = { 0.days }
                            )
                            suggest.info.timestamp.epochSeconds + equalsFromBeginningWeight.inWholeSeconds
                        }
                        .asSequence()
                        .distinctBy(CommentSuggest::normalized)

                    val suggests = allSuggests
                        .filter { it.suitableType != CommentSuggest.SuitableType.Absolute }
                        .take(16)
                        .map { it.info.comment }
                        .toList()

                    val category: CategoryInfo? = allSuggests
                        .firstOrNull { suggest ->
                            when (suggest.suitableType) {
                                CommentSuggest.SuitableType.Absolute,
                                CommentSuggest.SuitableType.IgnoreCase,
                                    -> true

                                CommentSuggest.SuitableType.FromBeginning,
                                CommentSuggest.SuitableType.Other,
                                    -> false
                            }
                        }
                        ?.info
                        ?.category

                    suggests to category
                }
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList<Comment>() to null,
            )

    val commentSuggests: StateFlow<List<Comment>> = commentSuggestsWithCalculatedCategory
        .mapStateLite(Pair<List<Comment>, *>::first)

    val category: StateFlow<CategoryInfo?> = skeleton.manualCategory.flatMapState(
        scope = scope,
    ) { manualOrNull ->
        manualOrNull.foldNullable(
            ifNotNull = { it.toMutableStateFlowAsInitial() },
            ifNull = { commentSuggestsWithCalculatedCategory.mapStateLite(Pair<*, CategoryInfo?>::second) }
        )
    }

    val record: StateFlow<Record?> = category
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