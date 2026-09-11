package org.hnau.pinfin.data.records

import kotlinx.serialization.Serializable
import org.hnau.pinfin.data.Record

interface RecordEntry<out C> {

    val record: Record<C>
}

@Serializable
data class SimpleRecord<out C>(
    override val record: Record<C>,
) : RecordEntry<C>

@Serializable
data class FilteredRecord<out C>(
    override val record: Record<C>,
    val included: Boolean,
) : RecordEntry<C>
