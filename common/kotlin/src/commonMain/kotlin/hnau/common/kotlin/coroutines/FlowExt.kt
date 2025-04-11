package hnau.common.kotlin.coroutines

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

suspend fun <T> Flow<T>.toStateLite(): StateFlow<T> {
    var lastCachedValue: T = first()
    val source: Flow<T> = this@toStateLite

    return object : StateFlow<T> {

        override val value: T
            get() = lastCachedValue

        override val replayCache: List<T>
            get() = listOf(value)

        override suspend fun collect(
            collector: FlowCollector<T>,
        ): Nothing {
            source.collect { newValue ->
                lastCachedValue = newValue
                collector.emit(newValue)
            }
            awaitCancellation()
        }

    }
}