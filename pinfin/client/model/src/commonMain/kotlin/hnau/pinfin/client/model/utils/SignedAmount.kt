package hnau.pinfin.client.model.utils

import arrow.core.Either
import hnau.pinfin.scheme.Amount
import hnau.pinfin.scheme.CategoryDirection
import hnau.pinfin.scheme.Record
import hnau.pinfin.scheme.Transaction
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue

@Serializable
@JvmInline
value class SignedAmount(
    val value: Long,
) {

    constructor(
        amount: Amount,
        positive: Boolean,
    ) : this(
        value = amount
            .value
            .toLong()
            .let { raw ->
                when (positive) {
                    true -> raw
                    false -> -raw
                }
            }
    )

    val amount: Amount
        get() = value.absoluteValue.toUInt().let(::Amount)

    val positive: Boolean
        get() = value >= 0

    operator fun plus(
        other: SignedAmount,
    ): SignedAmount = SignedAmount(
        value = value + other.value,
    )

    companion object {

        val zero: SignedAmount = SignedAmount(
            value = 0L,
        )
    }
}

val Record.signedAmount: SignedAmount
    get() = SignedAmount(
        amount = amount,
        positive = when (category.direction) {
            CategoryDirection.Credit -> true
            CategoryDirection.Debit -> false
        }
    )

val Transaction.signedAmountOrAmount: Either<SignedAmount, Amount>
    get() = when (val type = type) {
        is Transaction.Type.Entry -> Either.Left(
            type.records.tail.fold(
                initial = type.records.head.signedAmount,
            ) { acc, record ->
                acc + record.signedAmount
            }
        )

        is Transaction.Type.Transfer -> Either.Right(type.amount)
    }