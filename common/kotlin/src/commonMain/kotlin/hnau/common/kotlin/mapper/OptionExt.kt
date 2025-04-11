package hnau.common.kotlin.mapper

import arrow.core.Option

fun <I, O> Mapper.Companion.option(
    base: Mapper<I, O>,
): Mapper<Option<I>, Option<O>> = Mapper(
    direct = { i -> i.map(base.direct) },
    reverse = { o -> o.map(base.reverse) },
)
