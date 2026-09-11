package org.hnau.pinfin.data.records

import kotlinx.serialization.KSerializer
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.serialization.MappingKSerializer
import org.hnau.pinfin.data.Record

class RecordEntrySerializer<C>(
    categorySerializer: KSerializer<C>,
) : MappingKSerializer<Record<C>, RecordEntry<C>>(
    base = Record.serializer(categorySerializer),
    mapper = Mapper(
        direct = ::SimpleRecord,
        reverse = RecordEntry<C>::record,
    )
)
