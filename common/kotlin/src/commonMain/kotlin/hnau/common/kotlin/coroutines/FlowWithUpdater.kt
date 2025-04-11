package hnau.common.kotlin.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class FlowWithUpdater<T>(
    val value: Flow<T>,
    val update: suspend (suspend T.() -> T) -> Unit,
)

inline fun <I, O> FlowWithUpdater<I>.map(
    crossinline direct: (I) -> O,
    crossinline reverse: suspend I.(O) -> I,
) = FlowWithUpdater(
    value = value.map { value -> direct(value) },
    update = { updater ->
        update {
            this
                .let(direct)
                .updater()
                .let { newOut -> reverse(newOut) }
        }
    },
)
