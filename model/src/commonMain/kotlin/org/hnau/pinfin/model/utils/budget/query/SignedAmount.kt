package org.hnau.pinfin.model.utils.budget.query

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.plus
import org.hnau.commons.kotlin.serialization.MappingKSerializer
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.fold
import kotlin.jvm.JvmInline

/** A signed amount of money, serialized as a decimal string, e.g. `"-15.00"`. */
@Serializable(SignedAmount.Serializer::class)
@JvmInline
value class SignedAmount(
    val value: BigDecimal,
) : Comparable<SignedAmount> {

    override fun compareTo(
        other: SignedAmount,
    ): Int = value.compareTo(other.value)

    object Serializer : MappingKSerializer<String, SignedAmount>(
        base = String.serializer(),
        mapper = Mapper(
            direct = String::toBigDecimal,
            reverse = BigDecimal::toStringExpanded,
        ) + Mapper(
            direct = ::SignedAmount,
            reverse = SignedAmount::value,
        ),
    )

    companion object {

        val zero: SignedAmount = SignedAmount(BigDecimal.ZERO)

        fun of(
            directioned: KeyValue<AmountDirection, Amount>,
        ): SignedAmount = directioned.key.fold(
            ifCredit = { directioned.value.value },
            ifDebit = { -directioned.value.value },
        ).let(::SignedAmount)

        fun creditMinusDebit(
            credit: Amount,
            debit: Amount,
        ): SignedAmount = SignedAmount(
            value = credit.value - debit.value,
        )
    }
}
