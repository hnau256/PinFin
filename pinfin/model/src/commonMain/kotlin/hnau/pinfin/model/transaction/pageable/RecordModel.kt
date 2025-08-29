@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.Amount
import hnau.pinfin.data.Comment
import hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import hnau.pinfin.model.transaction.utils.allRecords
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class RecordModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    private val remove: StateFlow<(() -> Unit)?>,
) {

    enum class Part {

        Comment, Category, Amount;

        companion object {

            val default: Part
                get() = Comment
        }
    }

    sealed interface PageType {

        val key: Int

        val goBackHandler: GoBackHandler

        data class Comment(
            val model: CommentModel.Page,
        ) : PageType {
            override val key: Int
                get() = 0

            override val goBackHandler: GoBackHandler
                get() = model.goBackHandler
        }

        data class Category(
            val model: ChooseOrCreateModel<CategoryInfo>,
        ) : PageType {
            override val key: Int
                get() = 1

            override val goBackHandler: GoBackHandler
                get() = model.goBackHandler
        }

        data class Amount(
            val model: AmountModel.Page,
        ) : PageType {
            override val key: Int
                get() = 2

            override val goBackHandler: GoBackHandler
                get() = model.goBackHandler
        }
    }

    @Pipe
    interface Dependencies {

        fun comment(): CommentModel.Dependencies

        fun category(): CategoryModel.Dependencies

        fun amount(): AmountWithDirectionModel.Dependencies

        fun page(): Page.Dependencies
    }

    @Serializable
    data class Skeleton(
        var page: Page.Skeleton? = null,
        val part: MutableStateFlow<Part> = Part.default.toMutableStateFlowAsInitial(),
        val comment: CommentModel.Skeleton,
        val category: CategoryModel.Skeleton,
        val amount: AmountWithDirectionModel.Skeleton,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                comment = CommentModel.Skeleton.createForNew(),
                category = CategoryModel.Skeleton.createForNew(),
                amount = AmountWithDirectionModel.Skeleton.createForNew(),
            )

            fun createForEdit(
                record: TransactionInfo.Type.Entry.Record,
            ): Skeleton = Skeleton(
                comment = CommentModel.Skeleton.createForEdit(
                    comment = record.comment,
                ),
                category = CategoryModel.Skeleton.createForEdit(
                    category = record.category,
                ),
                amount = AmountWithDirectionModel.Skeleton.createForEdit(
                    amount = record.amount,
                ),
            )
        }
    }

    private fun switchToPart(
        part: Part,
    ) {
        skeleton.part.value = part
    }

    private fun createRequestFocus(
        part: Part,
    ): () -> Unit = { switchToPart(part) }

    private fun isPartFocused(
        part: Part,
    ): StateFlow<Boolean> = skeleton
        .part
        .mapState(scope) { it == part }

    val comment = CommentModel(
        scope = scope,
        dependencies = dependencies.comment(),
        skeleton = skeleton.comment,
        isFocused = isPartFocused(Part.Comment),
        requestFocus = createRequestFocus(Part.Comment),
        extractSuggests = { state ->
            state
                .allRecords
                .flatMap { (timestamp, record) ->
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
                            Comment(comment) to timestamp
                        }
                }
        }
    )

    val category = CategoryModel(
        scope = scope,
        dependencies = dependencies.category(),
        skeleton = skeleton.category,
        isFocused = isPartFocused(Part.Category),
        requestFocus = createRequestFocus(Part.Category),
        comment = comment.comment,
    )

    val amount = AmountWithDirectionModel(
        scope = scope,
        dependencies = dependencies.amount(),
        skeleton = skeleton.amount,
        isFocused = isPartFocused(Part.Amount),
        requestFocus = createRequestFocus(Part.Amount),
        category = category.category,
    )

    class Page(
        scope: CoroutineScope,
        dependencies: Dependencies,
        skeleton: Skeleton,
        val comment: CommentModel,
        val category: CategoryModel,
        val amount: AmountWithDirectionModel,
        val page: StateFlow<PageType>,
        val remove: StateFlow<(() -> Unit)?>,
    ) {

        @Pipe
        interface Dependencies

        @Serializable
        /*data*/ class Skeleton

        val goBackHandler: GoBackHandler =
            page.flatMapState(scope, PageType::goBackHandler)
    }

    fun createPage(
        scope: CoroutineScope,
        usedCategories: StateFlow<Set<CategoryInfo>>,
    ): Page = Page(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::page
            .toAccessor()
            .getOrInit { Page.Skeleton() },
        remove = remove,
        comment = comment,
        category = category,
        amount = amount,
        page = skeleton
            .part
            .mapWithScope(scope) { pageScope, part ->
                when (part) {

                    Part.Comment -> PageType.Comment(
                        model = comment.createPage(
                            scope = pageScope,
                        ),
                    )

                    Part.Category -> PageType.Category(
                        model = category.createPage(
                            scope = pageScope,
                            usedCategories = usedCategories,
                        ),
                    )

                    Part.Amount -> PageType.Amount(
                        model = amount.createPage(
                            scope = pageScope,
                        ),
                    )
                }
            },
    )

    val amountOrZero: StateFlow<Amount> = amount
        .amount
        .mapState(scope) { it ?: Amount.zero }

    val record: StateFlow<TransactionInfo.Type.Entry.Record?> = comment
        .comment
        .scopedInState(scope)
        .flatMapState(scope) { (commentScope, comment) ->
            createRecordFromComment(
                scope = commentScope,
                comment = comment,
            )
        }

    private fun createRecordFromComment(
        scope: CoroutineScope,
        comment: Comment,
    ): StateFlow<TransactionInfo.Type.Entry.Record?> = category
        .category
        .scopedInState(scope)
        .flatMapState(scope) { (categoryScope, categoryOrNull) ->
            categoryOrNull.foldNullable(
                ifNull = { null.toMutableStateFlowAsInitial() },
                ifNotNull = { category ->
                    createRecordFromCommentAndCategory(
                        scope = categoryScope,
                        comment = comment,
                        category = category,
                    )
                }
            )
        }

    private fun createRecordFromCommentAndCategory(
        scope: CoroutineScope,
        comment: Comment,
        category: CategoryInfo,
    ): StateFlow<TransactionInfo.Type.Entry.Record?> = amount
        .amountModel
        .amount
        .mapState(scope) { amountOrNull ->
            amountOrNull?.let { amount ->
                TransactionInfo.Type.Entry.Record(
                    amount = amount,
                    comment = comment,
                    category = category,
                )
            }
        }

    private fun Part.shift(
        offset: Int,
    ): Part? = Part
        .entries
        .getOrNull(ordinal + offset)

    val goBackHandler: GoBackHandler = skeleton
        .part
        .scopedInState(scope)
        .flatMapState(scope) { (partScope, part) ->
            when (part) {
                Part.Comment -> comment.goBackHandler
                Part.Category -> category.goBackHandler
                Part.Amount -> amount.goBackHandler
            }.mapState(partScope) { partGoBackOrNull ->
                partGoBackOrNull ?: part
                    .shift(-1)
                    ?.let { previousPart ->
                        { switchToPart(previousPart) }
                    }
            }
        }
}