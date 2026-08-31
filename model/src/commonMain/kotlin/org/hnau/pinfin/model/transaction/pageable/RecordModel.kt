@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.pageable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.combineEditableWith
import org.hnau.commons.app.model.utils.valueOrNone
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.combineStateWith
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapState
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Comment
import org.hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import org.hnau.pinfin.model.transaction.utils.allRecords
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo

class RecordModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    val goForward: () -> Unit,
    private val remove: StateFlow<(() -> Unit)?>,
) {

    @Fold
    enum class Part {

        Comment, Category, Amount;

        companion object {

            val default: Part
                get() = Comment
        }
    }

    @Fold
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
            val model: ChooseOrCreateModel<KeyValue<CategoryId, CategoryInfo>>,
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

        val budgetRepository: BudgetRepository

        fun comment(): CommentModel.Dependencies

        fun category(): CategoryModel.Dependencies

        fun amount(): AmountModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val part: MutableStateFlow<Part> = Part.default.toMutableStateFlowAsInitial(),
        val comment: CommentModel.Skeleton,
        val category: CategoryModel.Skeleton,
        val amount: AmountModel.Skeleton,
    ) {

        @Fold
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
                amount = AmountModel.Skeleton.createForNew(),
            )

            fun createForEdit(
                record: TransactionInfo.Type.Entry.Record,
            ): Skeleton = Skeleton(
                comment = CommentModel.Skeleton.createForEdit(
                    comment = record.comment,
                ),
                category = CategoryModel.Skeleton.createForEdit(
                    idWithCategory = record.idWithCategory,
                ),
                amount = AmountModel.Skeleton.createForEdit(
                    amount = record.amount,
                ),
            )
        }
    }

    private val selectedCategoryWrapper: MutableStateFlow<StateFlow<KeyValue<CategoryId, CategoryInfo>?>> =
        null.toMutableStateFlowAsInitial().toMutableStateFlowAsInitial()

    private val part: StateFlow<Part> = skeleton
        .part
        .flatMapWithScope(scope) { scope, part ->
            part.fold(
                ifSimple = { part -> part.toMutableStateFlowAsInitial() },
                ifAfterComment = {
                    selectedCategoryWrapper
                        .flatMapWithScope(scope) { scope, category ->
                            category.mapState(scope) { categoryOrNull ->
                                categoryOrNull.foldNullable(
                                    ifNull = { Part.Category },
                                    ifNotNull = { Part.Amount },
                                )
                            }
                        }
                },
            )
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

    val amount = AmountModel(
        scope = scope,
        dependencies = dependencies.amount(),
        skeleton = skeleton.amount,
        isFocused = isPartFocused(Part.Amount),
        requestFocus = createRequestFocus(Part.Amount),
        goForward = createGoForward(Part.Amount),
    )

    val categoryWithAmount: StateFlow<Pair<KeyValue<CategoryId, CategoryInfo>, Amount>?> = category
        .categoryEditable
        .flatMapWithScope(scope) { scope, categoryOrIncorrect ->
            when (categoryOrIncorrect) {
                Editable.Incorrect -> null.toMutableStateFlowAsInitial()
                is Editable.Value<KeyValue<CategoryId, CategoryInfo>> -> amount
                    .amountEditable
                    .flatMapWithScope(scope) { scope, amountOrNull ->
                        amountOrNull
                            .valueOrNone
                            .getOrNull()
                            .foldNullable(
                                ifNull = { null.toMutableStateFlowAsInitial() },
                                ifNotNull = { amountExpression ->
                                    dependencies.budgetRepository.state
                                        .mapState(scope) { state ->
                                            categoryOrIncorrect.value to amountExpression.toAmount(
                                                state.info.currency.scale
                                            )
                                        }
                                }
                            )
                    }
            }
        }

    class Page(
        scope: CoroutineScope,
        val comment: CommentModel,
        val category: CategoryModel,
        val amount: AmountModel,
        val page: StateFlow<PageType>,
        val remove: StateFlow<(() -> Unit)?>,
    ) {

        val goBackHandler: GoBackHandler =
            page.flatMapState(scope, PageType::goBackHandler)
    }

    fun createPage(
        scope: CoroutineScope,
        usedCategories: StateFlow<List<KeyValue<CategoryId, CategoryInfo>>>,
    ): Page = Page(
        scope = scope,
        remove = remove,
        comment = comment,
        category = category,
        amount = amount,
        page = part
            .mapWithScope(scope) { scope, part ->
                part.fold(
                    ifComment = {
                        PageType.Comment(
                            model = comment.createPage(
                                scope = scope,
                            ),
                        )
                    },
                    ifCategory = {
                        PageType.Category(
                            model = category.createPage(
                                scope = scope,
                                usedCategories = usedCategories,
                            ),
                        )
                    },
                    ifAmount = {
                        PageType.Amount(
                            model = amount.createPage(),
                        )
                    },
                )
            },
    )

    val amountOrZero: StateFlow<KeyValue<AmountDirection, Amount>> = amount
        .amountEditable
        .flatMapWithScope(scope) { scope, editable ->
            editable
                .valueOrNone
                .getOrNull()
                .foldNullable(
                    ifNull = { Amount.zero.toMutableStateFlowAsInitial() },
                    ifNotNull = { expression ->
                        dependencies.budgetRepository.state
                            .mapState(scope) { state ->
                                expression.toAmount(state.info.currency.scale)
                            }
                    }
                )
        }
        .combineStateWith(
            scope = scope,
            other = category.category,
        ) { amount, categoryOrNull ->
            KeyValue(
                key = categoryOrNull
                    ?.key
                    ?.direction
                    ?: AmountDirection.default,
                value = amount
            )
        }

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
                idWithCategory = category,
            )
        }

    private fun Part.shift(
        offset: Int,
    ): Part? = Part
        .entries
        .getOrNull(ordinal + offset)

    val goBackHandler: GoBackHandler = part.flatMapWithScope(scope) { scope, part ->
        part.fold(
            ifComment = { comment.goBackHandler },
            ifCategory = { category.goBackHandler },
            ifAmount = { amount.goBackHandler },
        ).mapState(scope) { partGoBackOrNull ->
            partGoBackOrNull ?: part
                .shift(-1)
                ?.let { previousPart ->
                    { switchToPart(previousPart) }
                }
        }
    }
}