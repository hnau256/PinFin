package hnau.common.kotlin.coroutines

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Suppress("FunctionName")
inline fun <T> StateFlowLite(
    flow: Flow<T>,
    crossinline getStateValue: () -> T,
): StateFlow<T> = object : StateFlow<T> {

    override val replayCache: List<T>
        get() = listOf(value)

    override val value: T
        get() = getStateValue()

    override suspend fun collect(
        collector: FlowCollector<T>,
    ): Nothing {
        flow
            .distinctUntilChanged()
            .collect(collector)
        awaitCancellation()
    }
}

fun <T> Flow<T>.toStateFlowLite(
    getStateValue: () -> T,
): StateFlow<T> = StateFlowLite(
    flow = this,
    getStateValue = getStateValue,
)
