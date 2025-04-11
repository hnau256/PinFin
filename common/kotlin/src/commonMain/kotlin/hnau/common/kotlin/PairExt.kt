package hnau.common.kotlin

inline fun <I, O> Pair<I, I>.map(
    transform: (I) -> O,
): Pair<O, O> = Pair(
    first = transform(first),
    second = transform(second),
)