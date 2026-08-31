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
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.combineStateWith
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.pinfin.data.expression.serialize
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository

class AmountModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

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
        .combineStateWith(
            scope = scope,
            other = dependencies.budgetRepository.state.mapState(scope) { it.info.currency },
        ) { input, currency ->
            AmountExpression.createOrNull(
                string = input.text,
                currency = currency,
            )
        }

    val error: StateFlow<Boolean> = amount.mapState(
        scope = scope,
    ) { it == null }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}