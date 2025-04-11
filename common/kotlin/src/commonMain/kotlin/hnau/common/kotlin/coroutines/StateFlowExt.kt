package hnau.common.kotlin.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.transformLatest

inline fun <I, O> StateFlow<I>.mapStateLite(
    crossinline transform: (I) -> O,
): StateFlow<O> {
    val cache = Reusable(transform)
    return this
        .transformLatest { emit(cache(it)) }
        .toStateFlowLite { cache(value) }
}

inline fun <I, O> StateFlow<I>.mapWithScope(
    scope: CoroutineScope,
    crossinline transform: (CoroutineScope, I) -> O,
): StateFlow<O> = this
    .scopedInState(scope)
    .mapState(scope) { (scope, value) -> transform(scope, value) }

fun <I, O> StateFlow<I>.mapState(
    scope: CoroutineScope,
    transform: (I) -> O,
): StateFlow<O> = this
    .mapStateLite(transform)
    .cacheStateInScope(scope)

@OptIn(ExperimentalCoroutinesApi::class)
inline fun <I, O> StateFlow<I>.flatMapStateLite(
    crossinline map: (I) -> StateFlow<O>,
): StateFlow<O> {
    val cache = Reusable(map)
    return this
        .flatMapLatest(cache)
        .toStateFlowLite { cache(value).value }
}

fun <I, O> StateFlow<I>.flatMapState(
    scope: CoroutineScope,
    map: (I) -> StateFlow<O>,
): StateFlow<O> = this
    .flatMapStateLite(map)
    .cacheStateInScope(scope)

fun <A, B, Z> combineState(
    scope: CoroutineScope,
    a: StateFlow<A>,
    b: StateFlow<B>,
    combine: (A, B) -> Z,
): StateFlow<Z> {
    val cache = Reusable<Pair<A, B>, Z> { (aValue, bValue) -> combine(aValue, bValue) }
    return a
        .combine(b) { aValue, bValue -> cache(aValue to bValue) }
        .toStateFlowLite { cache(a.value to b.value) }
        .cacheStateInScope(scope)
}

fun <A, B, Z> StateFlow<A>.combineStateWith(
    scope: CoroutineScope,
    other: StateFlow<B>,
    combine: (A, B) -> Z,
): StateFlow<Z> = combineState(
    scope = scope,
    a = this,
    b = other,
    combine = combine,
)
