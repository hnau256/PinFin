package hnau.common.kotlin.mapper

import kotlin.reflect.KClass

data class TypedMapper<I, O : Any>(
    val outClass: KClass<O>,
    val mapper: Mapper<I, O>,
)

inline fun <I, reified O : Any> Mapper<I, O>.toTyped(): TypedMapper<I, O> = TypedMapper(
    outClass = O::class,
    mapper = this,
)
