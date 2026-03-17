package org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period

import arrow.core.some
import arrow.core.toOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.EditingString
import org.hnau.commons.app.model.toEditingString
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.pinfin.model.transaction.utils.Editable

class NonNegativeCountModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
) {

    @Serializable
    data class Skeleton(
        val initial: Int,
        val manual: MutableStateFlow<EditingString> = initial
            .toString()
            .toEditingString()
            .toMutableStateFlowAsInitial(),
    )

    val manual: MutableStateFlow<EditingString>
        get() = skeleton.manual

    private fun generateShift(
        shift: Int,
    ): StateFlow<(() -> Unit)?> = skeleton
        .manual
        .mapState(scope) { manual ->
            manual
                .text
                .toIntOrNull()
                ?.let { it + shift }
                ?.takeIf { it >= 0 }
                ?.let { next ->
                    { skeleton.manual.value = next.toString().toEditingString() }
                }
        }

    val inc: StateFlow<(() -> Unit)?> = generateShift(1)

    val dec: StateFlow<(() -> Unit)?> = generateShift(-1)

    internal val countEditable: StateFlow<Editable<Int>> = Editable.create(
        scope = scope,
        valueOrNone = skeleton
            .manual
            .mapState(scope) { manual ->
                manual
                    .text
                    .toIntOrNull()
                    ?.takeIf { it >= 0 }
                    .toOption()
            },
        initialValueOrNone = skeleton.initial.some(),
    )

    val isCorrect: StateFlow<Boolean> = countEditable.mapState(scope) { countEditable ->
        when (countEditable) {
            Editable.Incorrect -> false
            is Editable.Value -> true
        }
    }
}