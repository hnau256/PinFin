package hnau.common.kotlin.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

fun <T> StateFlow<T>.stateInScope(
    scope: CoroutineScope,
): StateFlow<T> {
    val result = MutableStateFlow(value)
    scope.launch { collect(result) }
    return result
}

fun <T> Flow<T>.shareInScope(
    scope: CoroutineScope,
): SharedFlow<T> {
    val result = MutableSharedFlow<T>()
    scope.launch { collect(result) }
    return result
}
