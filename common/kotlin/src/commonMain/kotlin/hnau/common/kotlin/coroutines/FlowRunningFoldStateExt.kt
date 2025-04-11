package hnau.common.kotlin.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

fun <I, O> StateFlow<I>.runningFoldState(
    scope: CoroutineScope,
    createInitial: (I) -> O,
    operation: suspend (O, I) -> O,
): StateFlow<O> {
    val result = MutableStateFlow(value to createInitial(value))
    scope.launch {
        collect { value ->
            val current = result.value
            val (previousValue, acc) = current
            val new = when (value) {
                previousValue -> current
                else -> value to operation(acc, value)
            }
            result.value = new
        }
    }
    return result.mapStateLite { it.second }
}

fun <T> StateFlow<T>.runningFoldState(
    scope: CoroutineScope,
    operation: suspend (T, T) -> T,
): StateFlow<T> = runningFoldState(
    scope = scope,
    createInitial = { it },
    operation = operation,
)

@PublishedApi
internal data class Skippable<T>(
    val skip: Boolean,
    val value: T,
)
