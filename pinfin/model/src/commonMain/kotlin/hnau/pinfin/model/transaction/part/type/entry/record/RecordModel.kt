@file:UseSerializers(
    NonEmptyListSerializer::class,
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.part.type.entry.record

import arrow.core.serialization.NonEmptyListSerializer
import hnau.common.app.model.EditingString
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.app.model.toEditingString
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.model.AmountModel
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class RecordModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        fun amount(): AmountModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val amount: AmountModel.Skeleton,
        val category: MutableStateFlow<CategoryInfo?>,
        val direction: MutableStateFlow<AmountDirection>,
        val comment: MutableStateFlow<EditingString>,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                amount = AmountModel.Skeleton.empty,
                category = null.toMutableStateFlowAsInitial(),
                direction = AmountDirection.default.toMutableStateFlowAsInitial(),
                comment = "".toEditingString().toMutableStateFlowAsInitial(),
            )

            fun createForEdit(
                record: TransactionInfo.Type.Entry.Record,
            ): Skeleton {
                val (direction, amount) = record.amount.splitToDirectionAndRaw()
                return Skeleton(
                    amount = AmountModel.Skeleton(
                        amount = amount,
                    ),
                    category = record.category.toMutableStateFlowAsInitial(),
                    direction = direction.toMutableStateFlowAsInitial(),
                    comment = record.comment.text.toEditingString().toMutableStateFlowAsInitial(),
                )
            }
        }
    }

    private val amount = AmountModel(
        scope = scope,
        dependencies = dependencies.amount(),
        skeleton = skeleton.amount,
    )

    val info = RecordInfo(
        amount =amount,
        category = skeleton.category,
        direction = skeleton.direction,
        comment = skeleton.comment,
    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}