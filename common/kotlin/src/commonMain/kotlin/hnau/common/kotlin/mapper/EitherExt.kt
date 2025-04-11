package hnau.common.kotlin.mapper

import arrow.core.Either

fun <T> Either.Left.Companion.valueMapper() =
    Mapper<T, Either.Left<T>>(
        direct = { value -> Either.Left(value) },
        reverse = { left -> left.value },
    )

fun <T> Either.Right.Companion.valueMapper() =
    Mapper<T, Either.Right<T>>(
        direct = { value -> Either.Right(value) },
        reverse = { right -> right.value },
    )
