package org.hnau.pinfin.data.records

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import kotlinx.serialization.KSerializer
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.serialization.MappingKSerializer
import org.hnau.pinfin.data.Record

class RecordsSerializer<C>(
    categorySerializer: KSerializer<C>,
) : MappingKSerializer<NonEmptyList<Record<C>>, Records<C>>(
    base = NonEmptyListSerializer(Record.serializer(categorySerializer)),
    mapper = Mapper(
        direct = ::SimpleRecords,
        reverse = Records<C>::records
    )
)