package hnau.common.kotlin.mapper

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T, I, O> ReadWriteProperty<T, I>.map(
    mapper: Mapper<I, O>,
) = object : ReadWriteProperty<T, O> {

    override fun getValue(thisRef: T, property: KProperty<*>) =
        mapper.direct(this@map.getValue(thisRef, property))

    override fun setValue(thisRef: T, property: KProperty<*>, value: O) =
        this@map.setValue(thisRef, property, mapper.reverse(value))
}
