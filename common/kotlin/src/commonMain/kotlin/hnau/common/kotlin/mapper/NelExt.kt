package hnau.common.kotlin.mapper

import arrow.core.Nel
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull

@Suppress("FunctionName")
fun <T> NelListMapper(): Mapper<List<T>, NonEmptyList<T>> = Mapper(
    direct = { it.toNonEmptyListOrNull()!! },
    reverse = Nel<T>::all,
)
