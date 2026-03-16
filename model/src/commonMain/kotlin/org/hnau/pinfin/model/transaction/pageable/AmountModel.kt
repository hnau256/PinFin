@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.pageable

import arrow.core.toOption
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.model.transaction.utils.Editable
import org.hnau.pinfin.model.transaction.utils.valueOrNone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.pinfin.model.AmountModel as CommonAmountModel

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