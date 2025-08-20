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
import hnau.pinfin.data.Comment
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
            val model: CategoryModel.Page,
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

        fun direction(): AmountDirectionModel.Dependencies

        fun amount(): AmountModel.Dependencies

        fun page(): Page.Dependencies
    }

    @Serializable
    data class Skeleton(
        var page: Page.Skeleton? = null,
        val part: MutableStateFlow<Part> = Part.default.toMutableStateFlowAsInitial(),
        val comment: CommentModel.Skeleton,
        val category: CategoryModel.Skeleton,
        val direction: AmountDirectionModel.Skeleton,
        val amount: AmountModel.Skeleton,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                comment = CommentModel.Skeleton.createForNew(),
                category = CategoryModel.Skeleton.createForNew(),
                direction = AmountDirectionModel.Skeleton.createForNew(),
                amount = AmountModel.Skeleton.createForNew(),
            )

            fun createForEdit(
                record: TransactionInfo.Type.Entry.Record,
            ): Skeleton {
                val (direction, amount) = record.amount.splitToDirectionAndRaw()
                return Skeleton(
                    comment = CommentModel.Skeleton.createForEdit(
                        comment = record.comment,
                    ),
                    category = CategoryModel.Skeleton.createForEdit(
                        category = record.category,
                    ),
                    direction = AmountDirectionModel.Skeleton.createForEdit(
                        direction = direction,
                    ),
                    amount = AmountModel.Skeleton.createForEdit(
                        amount = amount,
                    ),
                )
            }
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
    )

    val category = CategoryModel(
        scope = scope,
        dependencies = dependencies.category(),
        skeleton = skeleton.category,
        isFocused = isPartFocused(Part.Category),
        requestFocus = createRequestFocus(Part.Category),
    )

    val direction = AmountDirectionModel(
        scope = scope,
        dependencies = dependencies.direction(),
        skeleton = skeleton.direction,
    )

    val amount = AmountModel(
        scope = scope,
        dependencies = dependencies.amount(),
        skeleton = skeleton.amount,
        isFocused = isPartFocused(Part.Amount),
        requestFocus = createRequestFocus(Part.Amount),
    )

    val pageType: StateFlow<Pair<Part, PageType>> = skeleton
        .part
        .mapWithScope(scope) { pageScope, part ->
            val pageType = when (part) {

                Part.Comment -> PageType.Comment(
                    model = comment.createPage(
                        scope = pageScope,
                    ),
                )

                Part.Category -> PageType.Category(
                    model = category.createPage(
                        scope = pageScope,
                    ),
                )

                Part.Amount -> PageType.Amount(
                    model = amount.createPage(
                        scope = pageScope,
                    ),
                )
            }

            part to pageType
        }

    class Page(
        scope: CoroutineScope,
        dependencies: Dependencies,
        skeleton: Skeleton,
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
    ): Page = Page(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::page
            .toAccessor()
            .getOrInit { Page.Skeleton() },
        remove = remove,
        page = skeleton
            .part
            .mapWithScope(scope) { partScope, part ->
                when (part) {
                    Part.Comment -> PageType.Comment(
                        model = comment.createPage(
                            scope = partScope,
                        ),
                    )

                    Part.Category -> PageType.Category(
                        model = category.createPage(
                            scope = partScope,
                        ),
                    )

                    Part.Amount -> PageType.Amount(
                        model = amount.createPage(
                            scope = partScope,
                        ),
                    )
                }
            },
    )

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

    val goBackHandler: GoBackHandler = pageType
        .scopedInState(scope)
        .flatMapState(scope) { (pageScope, partWithPage) ->
            val (part, pageModel) = partWithPage
            pageModel.goBackHandler
                .scopedInState(pageScope)
                .flatMapState(pageScope) { (goBackScope, goBack) ->
                    goBack.foldNullable(
                        ifNotNull = { it.toMutableStateFlowAsInitial() },
                        ifNull = {
                            when (part) {
                                Part.Comment -> comment.goBackHandler
                                Part.Category -> category.goBackHandler
                                Part.Amount -> amount.goBackHandler
                            }.mapState(goBackScope) { partGoBackOrNull ->
                                partGoBackOrNull ?: part
                                    .shift(-1)
                                    ?.let { previousPart ->
                                        { switchToPart(previousPart) }
                                    }
                            }
                        },
                    )
                }
        }
}