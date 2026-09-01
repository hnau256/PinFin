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
value class Amount private constructor(
    val value: BigDecimal,
) : Comparable<Amount> {

    object Serializer : MappingKSerializer<String, Amount>(
        base = String.serializer(),
        mapper = Mapper(
            direct = String::toBigDecimal,
            reverse = BigDecimal::toStringExpanded
        ) + Mapper(
            direct = ::Amount,
            reverse = Amount::value,
        ),
    )

    operator fun plus(
        other: Amount,
    ): Amount = Amount(
        value = value + other.value,
    )

    override fun compareTo(
        other: Amount,
    ): Int = value.compareTo(other.value)

    companion object {

        val zero: Amount = Amount(
            value = BigDecimal.ZERO,
        )

        @Deprecated("Use createOrNull instead")
        fun createUnsafe(
            value: BigDecimal,
        ): Amount = Amount(
            value = value,
        )

        @Suppress("DEPRECATION")
        fun createOrNull(
            value: BigDecimal,
        ): Amount? = value
            .takeIf { it >= BigDecimal.ZERO }
            ?.let(::createUnsafe)
    }
}

fun Iterable<Amount>.sum(): Amount = fold(
    initial = Amount.zero,
    operation = Amount::plus,
)