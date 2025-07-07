package hnau.pinfin.model.utils.budget.state

import arrow.core.Either
import hnau.pinfin.data.Amount
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.data.Record
import hnau.pinfin.data.Transaction
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue

@Serializable
@JvmInline
value class SignedAmount(
    val value: Long,
) {

    constructor(
        amount: Amount,
        direction: AmountDirection,
    ) : this(
        value = amount
            .value
            .toLong()
            .let { raw ->
                when (direction) {
                    AmountDirection.Credit -> raw
                    AmountDirection.Debit -> -raw
                }
            }
    )

    val amount: Amount
        get() = value.absoluteValue.toUInt().let(::Amount)

    val direction: AmountDirection
        get() = when (value >= 0) {
            true -> AmountDirection.Credit
            false -> AmountDirection.Debit
        }

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
        direction = category.id.direction
    )

val Record.signedAmount: SignedAmount
    get() = SignedAmount(
        amount = amount,
        direction = category.direction,
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