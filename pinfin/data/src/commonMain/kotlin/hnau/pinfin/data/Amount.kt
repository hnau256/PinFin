package hnau.pinfin.data

import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.stringToInt
import hnau.common.kotlin.serialization.MappingKSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlin.jvm.JvmInline

@JvmInline
@Serializable(Amount.Serializer::class)
value class Amount(
    val value: Int,
) {

    object Serializer : MappingKSerializer<String, Amount>(
        base = String.serializer(),
        mapper = Mapper.stringToInt + Mapper(::Amount, Amount::value)
    )

    fun splitToDirectionAndRaw(): Pair<AmountDirection, Amount> = when {
        value >= 0 -> AmountDirection.Credit to this
        else -> AmountDirection.Debit to -this
    }

    operator fun unaryMinus(): Amount = Amount(
        value = -value,
    )

    operator fun plus(
        other: Amount,
    ): Amount = Amount(
        value = value + other.value,
    )

    companion object {

        val zero: Amount = Amount(
            value = 0,
        )
    }
}