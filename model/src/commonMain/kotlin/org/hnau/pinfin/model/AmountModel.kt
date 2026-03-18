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
import org.hnau.commons.app.model.toEditingString
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.pinfin.data.expression.Expression
import org.hnau.pinfin.data.expression.parseOrNull
import org.hnau.pinfin.data.expression.serialize

class AmountModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
) {

    @Serializable
    data class Skeleton(
        val input: MutableStateFlow<EditingString>,
    ) {

        constructor(
            amount: AmountExpression,
        ) : this(
            input = amount
                .expression
                .serialize()
                .toEditingString()
                .toMutableStateFlowAsInitial()
        )

        companion object {

            val empty: Skeleton
                get() = Skeleton(
                    input = EditingString().toMutableStateFlowAsInitial(),
                )
        }
    }

    val input: MutableStateFlow<EditingString>
        get() = skeleton.input

    val amount: StateFlow<AmountExpression?> = skeleton
        .input
        .mapState(
            scope = scope,
        ) { input ->
            Expression
                .parseOrNull(input.text)
                ?.let(::AmountExpression)
        }

    val error: StateFlow<Boolean> = amount.mapState(
        scope = scope,
    ) { it == null }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}