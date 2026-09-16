package org.hnau.pinfin.model.transaction.edit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.editable
import org.hnau.commons.app.model.utils.valueOrNone
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Comment
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.model.transaction.edit.utils.EditNavigateContext
import org.hnau.pinfin.model.transaction.edit.utils.SelectedPartDelegate
import org.hnau.pinfin.model.transaction.utils.allRecords
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

class TransactionRecordEditModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    val remove: StateFlow<(() -> Unit)?>,
    navigateContext: EditNavigateContext,
) {


    enum class Part {

        Comment, Category, Amount;

        companion object {

            val default: Part
                get() = Comment
        }
    }

    @Pipe
    interface Dependencies {

        fun comment(): CommentEditModel.Dependencies

        fun category(): CategoryChooseModel.Dependencies

        fun amount(): AmountEditModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val comment: CommentEditModel.Skeleton,
        val category: CategoryChooseModel.Skeleton,
        val amount: AmountEditModel.Skeleton,
        val selectedPart: MutableStateFlow<Part> = Part.default.toMutableStateFlowAsInitial(),
    ) {

        @Fold
        @Serializable
        sealed interface Part {

            @Serializable
            @SerialName("simple")
            data class Simple(
                val part: TransactionRecordEditModel.Part,
            ) : Part

            @Serializable
            @SerialName("after_comment")
            data object AfterComment : Part

            companion object {

                val default: Part =
                    Simple(TransactionRecordEditModel.Part.default)
            }
        }

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                comment = CommentEditModel.Skeleton.createForNew(),
                category = CategoryChooseModel.Skeleton.createForNew(),
                amount = AmountEditModel.Skeleton.createForNew(),
            )

            fun create(
                record: Record<KeyValue<CategoryId, CategoryInfo>>,
            ): Skeleton = Skeleton(
                comment = CommentEditModel.Skeleton.create(
                    initial = record.comment,
                ),
                category = CategoryChooseModel.Skeleton.create(
                    idWithCategory = record.category,
                ),
                amount = AmountEditModel.Skeleton.create(
                    expression = record.amount,
                ),
            )
        }
    }

    private val selectedCategoryWrapper: MutableStateFlow<StateFlow<KeyValue<CategoryId, CategoryInfo>?>> =
        null.toMutableStateFlowAsInitial().toMutableStateFlowAsInitial()

    private val part: StateFlow<Part> = skeleton
        .selectedPart
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

    private val selectedPart: SelectedPartDelegate<Part> = SelectedPartDelegate.create(
        scope = scope,
        selectedPart = part,
        onPartChanged = { skeleton.selectedPart.value = Skeleton.Part.Simple(it) },
        navigateContext = navigateContext,
    )

    val comment = CommentEditModel(
        scope = scope,
        dependencies = dependencies.comment(),
        skeleton = skeleton.comment,
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
        navigateContext = selectedPart.createPartNavigateContext(Part.Comment),
    )

    val category = CategoryChooseModel(
        scope = scope,
        dependencies = dependencies.category(),
        skeleton = skeleton.category,
        commentToFindDefault = comment.commentEditable.mapState(
            scope = scope,
            transform = Editable.Value<Comment>::value,
        ),
        navigateContext = selectedPart.createPartNavigateContext(Part.Category),
    ).also { category ->
        selectedCategoryWrapper.value = category
            .categoryEditable
            .mapState(scope) { categoryInfoOrIncorrect ->
                categoryInfoOrIncorrect
                    .valueOrNone
                    .getOrNull()
            }
    }

    val amount = AmountEditModel(
        scope = scope,
        dependencies = dependencies.amount(),
        skeleton = skeleton.amount,
        navigateContext = selectedPart.createPartNavigateContext(Part.Amount),
    )

    val recordEditable: StateFlow<Editable<Record<KeyValue<CategoryId, CategoryInfo>>>> =
        derivedStateFlowOf(scope) {
            editable {
                Record(
                    category = category.categoryEditable.state.bind(),
                    comment = comment.commentEditable.state.bind(),
                    amount = amount.amountEditable.state.bind(),
                )
            }
        }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}