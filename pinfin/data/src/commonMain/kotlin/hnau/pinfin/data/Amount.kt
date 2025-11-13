package hnau.pinfin.data

import hnau.common.kotlin.KeyValue
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
) : Comparable<Amount> {

    object Serializer : MappingKSerializer<String, Amount>(
        base = String.serializer(),
        mapper = Mapper.stringToInt + Mapper(::Amount, Amount::value)
    )

    fun splitToDirectionAndRaw(): KeyValue<AmountDirection, Amount> = when {
        value >= 0 -> KeyValue(AmountDirection.Credit, this)
        else -> KeyValue(AmountDirection.Debit, -this)
    }

    fun withDirection(
        direction: AmountDirection,
    ): Amount = when (direction) {
        AmountDirection.Credit -> this
        AmountDirection.Debit -> {
            val (currentDirection, raw) = splitToDirectionAndRaw()
            when (currentDirection) {
                AmountDirection.Debit -> raw
                AmountDirection.Credit -> -this
            }
        }
    }

    operator fun unaryMinus(): Amount = Amount(
        value = -value,
    )

    operator fun plus(
        other: Amount,
    ): Amount = Amount(
        value = value + other.value,
    )

    operator fun minus(
        other: Amount,
    ): Amount = Amount(
        value = value - other.value,
    )

    override fun compareTo(
        other: Amount,
    ): Int = value.compareTo(other.value)

    companion object {

        val zero: Amount = Amount(
            value = 0,
        )
    }
}