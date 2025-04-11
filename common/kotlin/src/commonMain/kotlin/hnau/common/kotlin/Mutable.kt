package hnau.common.kotlin

import kotlinx.serialization.Serializable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Serializable
data class Mutable<T>(
    var value: T,
) : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

inline fun <T : Any> Mutable<T?>.getOrPutIfNull(createValue: () -> T): T =
    value ?: createValue().also { value = it }

inline fun <T, reified S : T> Mutable<T>.getOrPutTyped(createValue: () -> S): S =
    value.castOrElse { createValue().also { value = it } }
