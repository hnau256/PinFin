@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.EditingString
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.Amount

class AmountModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
) {

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

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}