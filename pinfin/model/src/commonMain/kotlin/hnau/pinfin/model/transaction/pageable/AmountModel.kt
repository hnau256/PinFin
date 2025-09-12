@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.Amount
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import hnau.pinfin.model.AmountModel as CommonAmountModel

class AmountModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    val goForward: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        fun common(): CommonAmountModel.Dependencies

        fun page(): Page.Dependencies
    }

    @Serializable
    data class Skeleton(
        var page: Page.Skeleton? = null,
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
        dependencies = dependencies.common(),
        skeleton = skeleton.delegate,
    )

    class Page(
        scope: CoroutineScope,
        dependencies: Dependencies,
        skeleton: Skeleton,
        val delegate: CommonAmountModel,
        val goForward: () -> Unit,
    ) {

        @Pipe
        interface Dependencies

        @Serializable
        /*data*/ class Skeleton

        val goBackHandler: GoBackHandler
            get() = delegate.goBackHandler
    }

    fun createPage(
        scope: CoroutineScope,
    ): Page = Page(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::page
            .toAccessor()
            .getOrInit { Page.Skeleton() },
        delegate = delegate,
        goForward = goForward,
    )

    val amount: StateFlow<Amount?>
        get() = delegate.amount

    val isChanged: StateFlow<Boolean> = skeleton.initialAmount.foldNullable(
        ifNull = { true.toMutableStateFlowAsInitial() },
        ifNotNull = { initial ->
            amount.mapState(scope) { current -> current != initial }
        }
    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}