@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.pageable

import arrow.core.toOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.valueOrNone
import org.hnau.pinfin.model.AmountModel as CommonAmountModel

class AmountModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    val goForward: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val currency: Currency
    }

    @Serializable
    data class Skeleton(
        val initialAmount: AmountExpression?,
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
                amount: AmountExpression,
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

    internal val amountEditable: StateFlow<Editable<AmountExpression>> = Editable.create(
        scope = scope,
        valueOrNone = delegate.amount.mapState(scope, AmountExpression?::toOption),
        initialValueOrNone = skeleton.initialAmount.toOption(),
    )

    val amount: StateFlow<Amount?> = amountEditable
        .mapState(scope) { editableAmountExpression ->
            editableAmountExpression
                .valueOrNone
                .getOrNull()
                ?.toAmount(dependencies.currency.scale)
        }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}