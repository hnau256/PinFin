package org.hnau.pinfin.model.transaction.edit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.editable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Comment
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

class TransactionRecordEditModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    val remove: StateFlow<(() -> Unit)?>,
) {

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
    ) {

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

    val comment = CommentEditModel(
        scope = scope,
        dependencies = dependencies.comment(),
        skeleton = skeleton.comment,
    )

    val category = CategoryChooseModel(
        scope = scope,
        dependencies = dependencies.category(),
        skeleton = skeleton.category,
        commentToFindDefault = comment.commentEditable.mapState(
            scope = scope,
            transform = Editable.Value<Comment>::value,
        ),
    )

    val amount = AmountEditModel(
        scope = scope,
        dependencies = dependencies.amount(),
        skeleton = skeleton.amount,
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