package hnau.common.kotlin.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

fun <T> StateFlow<T>.cacheStateInScope(
    scope: CoroutineScope,
): StateFlow<T> {
    val result = MutableStateFlow(value)
    scope.launch { collect(result) }
    return result
}
