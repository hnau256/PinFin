package org.hnau.pinfin.data

import arrow.core.NonEmptyList
import org.hnau.commons.kotlin.KeyValue

inline fun <T : Comparable<T>> KeyValue<AmountDirection, T>.plus(
    other: KeyValue<AmountDirection, T>,
    plus: (T, T) -> T,
    minus: (T, T) -> T,
): KeyValue<AmountDirection, T> = when {
    key == other.key -> KeyValue(key, plus(value, other.value))
    value >= other.value -> KeyValue(key, minus(value, other.value))
    else -> KeyValue(other.key, minus(other.value, value))
}

inline fun <T : Comparable<T>> NonEmptyList<KeyValue<AmountDirection, T>>.sum(
    plus: (T, T) -> T,
    minus: (T, T) -> T,
): KeyValue<AmountDirection, T> = tail.fold(
    initial = head,
) { acc, value ->
    acc.plus(
        other = value,
        plus = plus,
        minus = minus,
    )
}

operator fun KeyValue<AmountDirection, Amount>.plus(
    other: KeyValue<AmountDirection, Amount>,
): KeyValue<AmountDirection, Amount> = plus(
    other = other,
    plus = { a, b -> Amount(a.value + b.value) },
    minus = { a, b -> Amount(a.value - b.value) },
)

fun NonEmptyList<KeyValue<AmountDirection, Amount>>.sum(): KeyValue<AmountDirection, Amount> = sum(
    plus = { a, b -> Amount(a.value + b.value) },
    minus = { a, b -> Amount(a.value - b.value) },
)