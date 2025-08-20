package hnau.pinfin.model.utils

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import hnau.common.kotlin.foldBoolean

//TODO to common-kotlin
inline fun <T> Iterable<T>.exclude(
    predicate: (T) -> Boolean,
): Pair<List<T>, T>? = this
    .fold<_, Pair<List<T>, Option<T>>>(
        initial = emptyList<T>() to None
    ) { (acc, excludedOrNone), item ->
        excludedOrNone.fold(
            ifSome = { (acc + item) to excludedOrNone },
            ifEmpty = {
                predicate(item).foldBoolean(
                    ifTrue = { acc to Some(item) },
                    ifFalse = { (acc + item) to None },
                )
            },
        )
    }
    .let { (remaining, excludedOrNone) ->
        excludedOrNone.fold(
            ifEmpty = { null },
            ifSome = { excluded -> remaining to excluded },
        )
    }