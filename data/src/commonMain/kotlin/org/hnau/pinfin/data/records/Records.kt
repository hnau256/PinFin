@file:UseSerializers(
    NonEmptyListSerializer::class,
)

package org.hnau.pinfin.data.records

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.pinfin.data.Record

interface Records<out C> {

    val records: NonEmptyList<Record<C>>
}

@Serializable
data class SimpleRecords<out C>(
    override val records: NonEmptyList<Record<C>>,
) : Records<C>

@Serializable
data class FilteredRecords<out C>(
    val main: NonEmptyList<Record<C>>,
    val additional: List<Record<C>>,
) : Records<C> {

    override val records: NonEmptyList<Record<C>>
        get() = main + additional
}
