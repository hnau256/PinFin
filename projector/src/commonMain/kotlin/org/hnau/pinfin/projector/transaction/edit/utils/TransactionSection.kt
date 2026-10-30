package org.hnau.pinfin.projector.transaction.edit.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTableScope
import org.hnau.commons.kotlin.coroutines.flow.state.combineState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.mapper.Mapper

data class TransactionSection<T>(
    val state: StateFlow<T>,
    val content: SLazyTableScope.(T) -> Unit,
) {

    companion object {

        fun immutable(
            content: SLazyTableScope.() -> Unit,
        ): TransactionSection<Unit> = TransactionSection(
            state = Unit.toMutableStateFlowAsInitial(),
            content = { content() }
        )

        fun <A, B, T> composed(
            scope: CoroutineScope,
            first: TransactionSection<A>,
            second: TransactionSection<B>,
            joiner: Mapper<Pair<A, B>, T>,
        ): TransactionSection<T> = TransactionSection(
            state = combineState(
                scope = scope,
                first = first.state,
                second = second.state,
            ) { firstState, secondState ->
                joiner.direct(firstState to secondState)
            },
            content = { state ->
                val (firstState, secondState) = state.let(joiner.reverse)
                first.content(this, firstState)
                second.content(this, secondState)
            },
        )
    }
}