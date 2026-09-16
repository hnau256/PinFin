package org.hnau.pinfin.model.transaction.edit

import arrow.core.toOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.ifNull
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.pinfin.data.expression.serialize
import org.hnau.pinfin.model.transaction.edit.utils.EditNavigateContext
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository

class AmountEditModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    val navigateContext: EditNavigateContext,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val initial: AmountExpression?,
        val input: MutableStateFlow<String> = initial
            ?.expression
            ?.serialize()
            .ifNull { "" }
            .toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                initial = null,
            )

            fun create(
                expression: AmountExpression,
            ): Skeleton = Skeleton(
                initial = expression,
            )
        }
    }

    val input: MutableStateFlow<String>
        get() = skeleton.input

    val amountEditable: StateFlow<Editable<AmountExpression>> = Editable.create(
        scope = scope,
        valueOrNone = derivedStateFlowOf(scope) {
            AmountExpression.createOrNull(
                string = input.state,
                currency = dependencies.budgetRepository.state.state.info.currency,
            ).toOption()
        },
        initialValueOrNone = skeleton.initial.toOption(),
    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}