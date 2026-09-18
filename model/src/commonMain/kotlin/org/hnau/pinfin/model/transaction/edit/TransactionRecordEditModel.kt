@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.edit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.editable
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.Comment
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.model.transaction.edit.utils.EditNavigateContext
import org.hnau.pinfin.model.transaction.edit.utils.SelectedPartDelegate
import org.hnau.pinfin.model.transaction.utils.allRecords
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.CategoryIdWithInfo

class TransactionRecordEditModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    val remove: StateFlow<(() -> Unit)?>,
    navigateContext: EditNavigateContext,
) {

    @Fold
    enum class Part {

        Comment, Category, Amount;

        companion object {

            val default: Part
                get() = Comment
        }
    }

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

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
                record: Record<CategoryIdWithInfo>,
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
        navigateContext = SelectedPartDelegate.create(
            scope = scope,
            selectedPart = resolvePart(
                scope = scope,
                actualCategory = null,
            ),
            onPartChanged = { skeleton.selectedPart.value = Skeleton.Part.Simple(it) },
            navigateContext = navigateContext,
        )
            .createPartNavigateContext(Part.Comment)
            .copy(
                goForward = { skeleton.selectedPart.value = Skeleton.Part.AfterComment },
            ),
    )

    private val categoryDelegate = CategoryChooseModel.Delegate(
        scope = scope,
        skeleton = skeleton.category,
        dependencies = dependencies.category(),
        comment = comment.commentEditable.mapState(scope) { it.value },
    )


    private fun resolvePart(
        scope: CoroutineScope,
        actualCategory: StateFlow<CategoryIdWithInfo?>?,
    ): StateFlow<Part> = skeleton
        .selectedPart
        .flatMapWithScope(scope) { scope, part ->
            part.fold(
                ifSimple = { it.toMutableStateFlowAsInitial() },
                ifAfterComment = {
                    actualCategory.foldNullable(
                        ifNull = {
                            Part.Category.toMutableStateFlowAsInitial()
                        },
                        ifNotNull = { category ->
                            category.mapState(scope) { categoryOrNull ->
                                categoryOrNull.foldNullable(
                                    ifNull = { Part.Category },
                                    ifNotNull = { Part.Amount },
                                )
                            }
                        }
                    )
                },
            )
        }


    private val effectivePart: StateFlow<Part> = resolvePart(
        scope = scope,
        actualCategory = categoryDelegate.actualCategory,
    )

    private val selectedPart: SelectedPartDelegate<Part> = SelectedPartDelegate.create(
        scope = scope,
        selectedPart = effectivePart,
        onPartChanged = { skeleton.selectedPart.value = Skeleton.Part.Simple(it) },
        navigateContext = navigateContext,
    )

    val category = CategoryChooseModel(
        delegate = categoryDelegate,
        navigateContext = selectedPart.createPartNavigateContext(Part.Category),
    )

    val amount = AmountEditModel(
        scope = scope,
        dependencies = dependencies.amount(),
        skeleton = skeleton.amount,
        navigateContext = selectedPart.createPartNavigateContext(Part.Amount),
    )

    val recordEditable: StateFlow<Editable<Record<CategoryIdWithInfo>>> =
        derivedStateFlowOf(scope) {
            editable {
                Record(
                    category = category.categoryEditable.state.bind(),
                    comment = comment.commentEditable.state.bind(),
                    amount = amount.amountEditable.state.bind(),
                )
            }
        }

    private fun Part.shift(
        offset: Int,
    ): Part? = Part
        .entries
        .getOrNull(ordinal + offset)

    val goBackHandler: GoBackHandler = derivedStateFlowOf(scope) {
        val currentPart = effectivePart.state
        currentPart
            .fold(
                ifComment = { comment.goBackHandler },
                ifCategory = { category.goBackHandler },
                ifAmount = { amount.goBackHandler },
            )
            .state
            ?: currentPart
                .shift(-1)
                ?.let { previousPart ->
                    { skeleton.selectedPart.value = Skeleton.Part.Simple(previousPart) }
                }
    }
}