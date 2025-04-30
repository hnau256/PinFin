package hnau.common.kotlin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

sealed class Loadable<out T> {

    companion object
}

data object Loading : Loadable<Nothing>()

data class Ready<out T>(val value: T) : Loadable<T>()

inline fun <I, O> Loadable<I>.fold(
    ifLoading: () -> O,
    ifReady: (I) -> O,
): O = when (this) {
    is Loading -> ifLoading()
    is Ready -> ifReady(value)
}

inline fun <I, O> Loadable<I>.map(
    transform: (I) -> O,
): Loadable<O> = fold(
    ifLoading = { Loading },
    ifReady = { transform(it).let(::Ready) },
)

inline fun <I, O> Loadable<I>.flatMap(
    transform: (I) -> Loadable<O>,
): Loadable<O> = fold(
    ifLoading = { Loading },
    ifReady = transform,
)

inline fun <T> Loadable<T>.valueOrElse(
    ifLoading: () -> T,
): T =
    fold(
        ifReady = { it },
        ifLoading = ifLoading,
    )

fun <T> LoadableStateFlow(
    scope: CoroutineScope,
    get: suspend () -> T,
): StateFlow<Loadable<T>> = flow {
    val value = get()
    emit(Ready(value))
}.stateIn(
    scope = scope,
    initialValue = Loading,
    started = SharingStarted.Eagerly,
)
