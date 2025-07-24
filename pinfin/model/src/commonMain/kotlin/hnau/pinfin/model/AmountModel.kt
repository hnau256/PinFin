@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model

import hnau.common.app.model.EditingString
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.Amount
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class AmountModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val state: MutableStateFlow<State>,
    ) {

        constructor(
            amount: Amount,
        ) : this(
            state = State(
                amount = amount,
                input = null,
            ).toMutableStateFlowAsInitial()
        )

        companion object {

            val empty: Skeleton
                get() = Skeleton(
                    state = State(
                        amount = null,
                        input = EditingString(),
                    ).toMutableStateFlowAsInitial(),
                )
        }
    }

    @Serializable
    data class State(
        val amount: Amount?,
        val input: EditingString?,
    )

    @Pipe
    interface Dependencies

    val state: MutableStateFlow<State>
        get() = skeleton.state

    val amount: StateFlow<Amount?> = skeleton
        .state
        .mapState(
            scope = scope,
        ) { state ->
            state.amount
        }

    val error: StateFlow<Boolean> = amount.mapState(
        scope = scope,
    ) { it == null }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}