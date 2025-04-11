package hnau.common.kotlin.serialization

import hnau.common.kotlin.mapper.Mapper
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

open class MappingKSerializer<I, O>(
    private val base: KSerializer<I>,
    private val mapper: Mapper<I, O>,
) : KSerializer<O> {

    override val descriptor: SerialDescriptor
        get() = base.descriptor

    override fun deserialize(
        decoder: Decoder,
    ) = base
        .deserialize(decoder)
        .let(mapper.direct)

    override fun serialize(
        encoder: Encoder,
        value: O,
    ) = value
        .let(mapper.reverse)
        .let { convertedValue ->
            base.serialize(encoder, convertedValue)
        }
}

fun <I, O> KSerializer<I>.map(
    mapper: Mapper<I, O>,
): KSerializer<O> = MappingKSerializer(
    base = this,
    mapper = mapper,
)
