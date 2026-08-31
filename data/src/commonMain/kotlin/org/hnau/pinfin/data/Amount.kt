package org.hnau.pinfin.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
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