package hnau.common.kotlin.coroutines

import hnau.common.kotlin.Loadable
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

fun <T> Deferred<T>.toLoadableStateFlow(
    lifecycleScope: CoroutineScope,
): StateFlow<Loadable<T>> = flow {
    val result = await()
    emit(result)
}
    .map(::Ready)
    .stateIn(
        scope = lifecycleScope,
        initialValue = Loading,
        started = SharingStarted.Eagerly,
    )
