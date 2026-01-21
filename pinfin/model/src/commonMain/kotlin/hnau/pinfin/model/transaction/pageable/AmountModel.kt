@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import arrow.core.toOption
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.flow.state.mapState
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.Amount
import hnau.pinfin.model.transaction.utils.Editable
import hnau.pinfin.model.transaction.utils.valueOrNone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import hnau.pinfin.model.AmountModel as CommonAmountModel

class AmountModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    val goForward: () -> Unit,
) {

    @Serializable
    data class Skeleton(
        val initialAmount: Amount?,
        val delegate: CommonAmountModel.Skeleton = initialAmount.foldNullable(
            ifNull = { CommonAmountModel.Skeleton.empty },
            ifNotNull = { amount ->
                CommonAmountModel.Skeleton(
                    amount = amount,
                )
            }
        ),
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                initialAmount = null,
            )

            fun createForEdit(
                amount: Amount,
            ): Skeleton = Skeleton(
                initialAmount = amount,
            )
        }
    }

    private val delegate: CommonAmountModel = CommonAmountModel(
        scope = scope,
        skeleton = skeleton.delegate,
    )

    class Page(
        val delegate: CommonAmountModel,
        val goForward: () -> Unit,
    ) {

        val goBackHandler: GoBackHandler
            get() = delegate.goBackHandler
    }

    fun createPage(): Page = Page(
        delegate = delegate,
        goForward = goForward,
    )

    internal val amountEditable: StateFlow<Editable<Amount>> = Editable.create(
        scope = scope,
        valueOrNone = delegate.amount.mapState(scope, Amount?::toOption),
        initialValueOrNone = skeleton.initialAmount.toOption(),
    )

    val amount: StateFlow<Amount?> = amountEditable
        .mapState(scope) { it.valueOrNone.getOrNull() }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}