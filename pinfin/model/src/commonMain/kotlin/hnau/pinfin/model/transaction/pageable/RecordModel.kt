@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import arrow.core.getOrElse
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.flatMapWithScope
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.Amount
import hnau.pinfin.data.Comment
import hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import hnau.pinfin.model.transaction.utils.Editable
import hnau.pinfin.model.transaction.utils.allRecords
import hnau.pinfin.model.transaction.utils.combineEditableWith
import hnau.pinfin.model.transaction.utils.valueOrNone
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class RecordModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    val goForward: () -> Unit,
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
    }

    @Serializable
    data class Skeleton(
        val part: MutableStateFlow<Part> = Part.default.toMutableStateFlowAsInitial(),
        val comment: CommentModel.Skeleton,
        val category: CategoryModel.Skeleton,
        val amount: AmountWithDirectionModel.Skeleton,
    ) {

        @Serializable
        sealed interface Part {

            @Serializable
            @SerialName("simple")
            data class Simple(
                val part: RecordModel.Part,
            ) : Part

            @Serializable
            @SerialName("after_comment")
            data object AfterComment : Part

            companion object {

                val default: Part =
                    Simple(RecordModel.Part.default)
            }
        }

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

    private val selectedCategoryWrapper: MutableStateFlow<StateFlow<CategoryInfo?>> =
        null.toMutableStateFlowAsInitial().toMutableStateFlowAsInitial()

    private val part: StateFlow<Part> = skeleton
        .part
        .flatMapWithScope(scope) { scope, part ->
            when (part) {
                is Skeleton.Part.Simple -> part.part.toMutableStateFlowAsInitial()
                Skeleton.Part.AfterComment -> selectedCategoryWrapper
                    .flatMapWithScope(scope) { scope, category ->
                        category.mapState(scope) { categoryOrNull ->
                            categoryOrNull.foldNullable(
                                ifNull = { Part.Category },
                                ifNotNull = { Part.Amount },
                            )
                        }
                    }
            }
        }

    private fun switchToPart(
        part: Part,
    ) {
        skeleton.part.value = Skeleton.Part.Simple(part)
    }

    private fun createRequestFocus(
        part: Part,
    ): () -> Unit = { switchToPart(part) }

    private fun isPartFocused(
        part: Part,
    ): StateFlow<Boolean> = this
        .part
        .mapState(scope) { it == part }

    private fun createGoForward(
        from: Part,
    ): () -> Unit = {
        from
            .shift(1)
            .foldNullable(
                ifNull = goForward,
                ifNotNull = ::switchToPart,
            )
    }

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
        },
        goForward = { skeleton.part.value = Skeleton.Part.AfterComment },
    )

    val category = CategoryModel(
        scope = scope,
        dependencies = dependencies.category(),
        skeleton = skeleton.category,
        isFocused = isPartFocused(Part.Category),
        requestFocus = createRequestFocus(Part.Category),
        comment = comment.commentEditable.mapState(scope, Editable.Value<Comment>::value),
        goForward = createGoForward(Part.Category),
    ).also { category ->
        selectedCategoryWrapper.value = category
            .categoryEditable
            .mapState(scope) { categoryInfoOrIncorrect ->
                categoryInfoOrIncorrect
                    .valueOrNone
                    .getOrNull()
            }
    }

    val amount = AmountWithDirectionModel(
        scope = scope,
        dependencies = dependencies.amount(),
        skeleton = skeleton.amount,
        isFocused = isPartFocused(Part.Amount),
        requestFocus = createRequestFocus(Part.Amount),
        category = category.categoryEditable.mapState(scope) { it.valueOrNone.getOrNull() },
        goForward = createGoForward(Part.Amount),
    )

    val categoryWithAmount: StateFlow<Pair<CategoryInfo, Amount>?> = category
        .categoryEditable
        .flatMapWithScope(scope) { scope, categoryOrIncorrect ->
            when (categoryOrIncorrect) {
                Editable.Incorrect -> null.toMutableStateFlowAsInitial()
                is Editable.Value<CategoryInfo> -> amount
                    .amountEditable
                    .mapState(scope) { amountOrNull ->
                        amountOrNull
                            .valueOrNone
                            .getOrNull()
                            ?.let { amount -> categoryOrIncorrect.value to amount }
                    }
            }
        }

    class Page(
        scope: CoroutineScope,
        val comment: CommentModel,
        val category: CategoryModel,
        val amount: AmountWithDirectionModel,
        val page: StateFlow<PageType>,
        val remove: StateFlow<(() -> Unit)?>,
    ) {

        val goBackHandler: GoBackHandler =
            page.flatMapState(scope, PageType::goBackHandler)
    }

    fun createPage(
        scope: CoroutineScope,
        usedCategories: StateFlow<Set<CategoryInfo>>,
    ): Page = Page(
        scope = scope,
        remove = remove,
        comment = comment,
        category = category,
        amount = amount,
        page = part
            .mapWithScope(scope) { scope, part ->
                when (part) {

                    Part.Comment -> PageType.Comment(
                        model = comment.createPage(
                            scope = scope,
                        ),
                    )

                    Part.Category -> PageType.Category(
                        model = category.createPage(
                            scope = scope,
                            usedCategories = usedCategories,
                        ),
                    )

                    Part.Amount -> PageType.Amount(
                        model = amount.createPage(),
                    )
                }
            },
    )

    val amountOrZero: StateFlow<Amount> = amount
        .amountEditable
        .mapState(scope) { it.valueOrNone.getOrElse { Amount.zero } }

    internal val record: StateFlow<Editable<TransactionInfo.Type.Entry.Record>> = comment
        .commentEditable
        .combineEditableWith(
            scope = scope,
            other = category.categoryEditable,
            combine = ::Pair,
        )
        .combineEditableWith(
            scope = scope,
            other = amount.amountEditable,
        ) { (comment, category), amount ->
            TransactionInfo.Type.Entry.Record(
                amount = amount,
                comment = comment,
                category = category,
            )
        }

    private fun Part.shift(
        offset: Int,
    ): Part? = Part
        .entries
        .getOrNull(ordinal + offset)

    val goBackHandler: GoBackHandler = part.flatMapWithScope(scope) { scope, part ->
        when (part) {
            Part.Comment -> comment.goBackHandler
            Part.Category -> category.goBackHandler
            Part.Amount -> amount.goBackHandler
        }.mapState(scope) { partGoBackOrNull ->
            partGoBackOrNull ?: part
                .shift(-1)
                ?.let { previousPart ->
                    { switchToPart(previousPart) }
                }
        }
    }
}