package org.hnau.pinfin.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.plus
import org.hnau.commons.kotlin.serialization.MappingKSerializer
import kotlin.jvm.JvmInline

@JvmInline
@Serializable(Amount.Serializer::class)
value class Amount(
    val value: BigDecimal,
) : Comparable<Amount> {

    object Serializer : MappingKSerializer<String, Amount>(
        base = String.serializer(),
        mapper = stringMapper,
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
            value = BigDecimal.ZERO,
        )

        val centsMapper: Mapper<Int, Amount> = Mapper<Int, BigDecimal>(
            direct = { it.toBigDecimal().div(100) },
            reverse = { it.times(100).intValue() }
        ) + Mapper(::Amount, Amount::value)

        val stringMapper: Mapper<String, Amount> = Mapper(
            direct = String::toBigDecimal,
            reverse = BigDecimal::toStringExpanded
        ) + Mapper(::Amount, Amount::value)
    }
}

fun Iterable<Amount>.sum(): Amount = fold(
    initial = Amount.zero,
    operation = Amount::plus,
)