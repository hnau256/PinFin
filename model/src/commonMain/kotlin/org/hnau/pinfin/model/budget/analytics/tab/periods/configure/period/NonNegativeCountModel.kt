@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.periods.configure.period

import arrow.core.some
import arrow.core.toOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.EditingString
import org.hnau.commons.app.model.toEditingString
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer

/**
 * Порт `graph.configure.period.NonNegativeCountModel` (см. docs/analytics-v2-plan.md, "2.6"):
 * положительное целое (минимум 1), редактируемое либо текстом, либо кнопками +/-.
 * Отдельная копия в пакете `periods`, чтобы не тянуть зависимость на старый `graph`,
 * который удаляется в фазе 5.
 */
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
                ?.takeIf { it >= 1 }
                ?.let { next ->
                    { skeleton.manual.value = next.toString().toEditingString() }
                }
        }

    val inc: StateFlow<(() -> Unit)?> = generateShift(1)

    val dec: StateFlow<(() -> Unit)?> = generateShift(-1)

    val bestEffortCount: StateFlow<Int> = skeleton
        .manual
        .mapState(scope) { manual ->
            manual
                .text
                .toIntOrNull()
                ?.takeIf { it >= 1 }
                ?: skeleton.initial
        }

    internal val countEditable: StateFlow<Editable<Int>> = Editable.create(
        scope = scope,
        valueOrNone = skeleton
            .manual
            .mapState(scope) { manual ->
                manual
                    .text
                    .toIntOrNull()
                    ?.takeIf { it >= 1 }
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
