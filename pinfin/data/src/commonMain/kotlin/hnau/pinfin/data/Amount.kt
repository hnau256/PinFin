package hnau.pinfin.data

import arrow.core.Either
import arrow.core.serialization.ArrowModule
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

    val direction: AmountDirection
        get() = when {
            value >= 0 -> AmountDirection.Credit
            else -> AmountDirection.Debit
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