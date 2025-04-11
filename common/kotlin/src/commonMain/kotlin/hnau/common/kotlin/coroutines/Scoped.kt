package hnau.common.kotlin.coroutines

import kotlinx.coroutines.CoroutineScope

data class Scoped<T>(
    val scope: CoroutineScope,
    val value: T,
)

inline fun <I, O> Scoped<I>.map(
    transform: (I) -> O,
): Scoped<O> = Scoped(
    scope = scope,
    value = value.let(transform),
)
