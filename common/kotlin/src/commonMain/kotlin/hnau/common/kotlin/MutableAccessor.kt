package hnau.common.kotlin

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import hnau.common.kotlin.mapper.Mapper
import kotlin.reflect.KMutableProperty0

data class MutableAccessor<T>(
    val get: () -> T,
    val set: (T) -> Unit,
)

fun <T> KMutableProperty0<T>.toAccessor(): MutableAccessor<T> = MutableAccessor(
    get = ::get,
    set = ::set,
)

fun <I, O> MutableAccessor<I>.map(
    mapper: Mapper<I, O>,
): MutableAccessor<O> = MutableAccessor(
    get = {
        val value = get()
        mapper.direct(value)
    },
    set = { valueToSet ->
        val transformedValue = mapper.reverse(valueToSet)
        set(transformedValue)
    }
)

inline fun <T, reified R : T> MutableAccessor<Option<T>>.shrinkType(): MutableAccessor<Option<R>> =
    MutableAccessor(
        get = {
            get().flatMap { value ->
                when (value) {
                    is R -> Some(value)
                    else -> None
                }
            }
        },
        set = set,
    )

inline fun <T> MutableAccessor<Option<T>>.getOrInitOption(
    init: () -> T,
): T = when (val valueOrNone = get()) {
    None -> init().also { set(Some(it)) }
    is Some -> valueOrNone.value
}

inline fun <T> MutableAccessor<T?>.getOrInit(
    init: () -> T,
): T = when (val valueOrNull = get()) {
    null -> init().also { set(it) }
    else -> valueOrNull
}