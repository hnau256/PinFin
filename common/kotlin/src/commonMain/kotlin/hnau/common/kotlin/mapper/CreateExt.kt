package hnau.common.kotlin.mapper

import hnau.common.kotlin.it

fun <T> Mapper.Companion.equality() = Mapper<T, T>(::it, ::it)

fun <I, O> Mapper.Companion.createPredeterminated(
    i: I,
    o: O,
) = Mapper<I, O>(
    direct = { o },
    reverse = { i },
)

fun <I, O> Mapper.Companion.nullable(
    mapper: Mapper<I, O>,
) = Mapper<I?, O?>(
    direct = { it?.let(mapper.direct) },
    reverse = { it?.let(mapper.reverse) },
)
