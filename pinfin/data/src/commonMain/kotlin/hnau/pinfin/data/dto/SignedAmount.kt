package hnau.pinfin.data.dto

import arrow.core.Either
import hnau.pinfin.data.repository.budget.TransactionInfo
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

val TransactionInfo.Type.Entry.Record.signedAmount: SignedAmount
    get() = SignedAmount(
        amount = amount,
        positive = when (category.id.direction) {
            CategoryDirection.Credit -> true
            CategoryDirection.Debit -> false
        }
    )

val Record.signedAmount: SignedAmount
    get() = SignedAmount(
        amount = amount,
        positive = when (category.direction) {
            CategoryDirection.Credit -> true
            CategoryDirection.Debit -> false
        }
    )

val Transaction.Type.Entry.signedAmount: SignedAmount
    get() = records.tail.fold(
        initial = records.head.signedAmount,
    ) { acc, record ->
        acc + record.signedAmount
    }

val TransactionInfo.Type.Entry.signedAmount: SignedAmount
    get() = records.tail.fold(
        initial = records.head.signedAmount,
    ) { acc, record ->
        acc + record.signedAmount
    }

val TransactionInfo.signedAmountOrAmount: Either<SignedAmount, Amount>
    get() = when (val type = type) {
        is TransactionInfo.Type.Entry -> Either.Left(type.signedAmount)
        is TransactionInfo.Type.Transfer -> Either.Right(type.amount)
    }