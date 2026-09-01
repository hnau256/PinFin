package org.hnau.pinfin.data

import arrow.core.NonEmptyList
import org.hnau.commons.kotlin.KeyValue

interface PlusMinus<T> {

    fun plus(
        a: T,
        b: T,
    ): T

    fun minus(
        a: T,
        b: T,
    ): T
}

fun <T : Comparable<T>> KeyValue<AmountDirection, T>.plus(
    other: KeyValue<AmountDirection, T>,
    plusMinus: PlusMinus<T>,
): KeyValue<AmountDirection, T> = when {
    key == other.key -> KeyValue(key, plusMinus.plus(value, other.value))
    value >= other.value -> KeyValue(key, plusMinus.minus(value, other.value))
    else -> KeyValue(other.key, plusMinus.minus(other.value, value))
}

fun <T : Comparable<T>> NonEmptyList<KeyValue<AmountDirection, T>>.sum(
    plusMinus: PlusMinus<T>,
): KeyValue<AmountDirection, T> = tail.fold(
    initial = head,
) { acc, value ->
    acc.plus(
        other = value,
        plusMinus = plusMinus,
    )
}

private val amountPlusMinus: PlusMinus<Amount> = object : PlusMinus<Amount> {

    override fun plus(
        a: Amount,
        b: Amount
    ): Amount = a + b

    @Suppress("DEPRECATION")
    override fun minus(
        a: Amount,
        b: Amount
    ): Amount = Amount.createUnsafe(
        value = a.value - b.value,
    )
}

operator fun KeyValue<AmountDirection, Amount>.plus(
    other: KeyValue<AmountDirection, Amount>,
): KeyValue<AmountDirection, Amount> = plus(
    other = other,
    plusMinus = amountPlusMinus,
)

fun NonEmptyList<KeyValue<AmountDirection, Amount>>.sum(): KeyValue<AmountDirection, Amount> = sum(
    plusMinus = amountPlusMinus,
)