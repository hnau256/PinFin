package hnau.pinfin.data

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrThrow
import hnau.common.gen.enumvalues.annotations.EnumValues
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AmountDirection {

    @SerialName("credit")
    Credit,

    @SerialName("debit")
    Debit,
    ;

    val opposite: AmountDirection
        get() = when (this) {
            Credit -> Debit
            Debit -> Credit
        }

    companion object {

        val default: AmountDirection = Debit

        val nonEmptyEntries: NonEmptyList<AmountDirection> =
            entries.toNonEmptyListOrThrow()
    }
}

@Serializable
data class CategoryDirectionValues<T>(
    val credit: T,
    val debit: T,
) {

    operator fun get(
        direction: AmountDirection,
    ): T = when (direction) {
        AmountDirection.Credit -> credit
        AmountDirection.Debit -> debit
    }

    companion object {

        inline fun <T> create(
            create: (AmountDirection) -> T,
        ): CategoryDirectionValues<T> = CategoryDirectionValues(
            credit = create(AmountDirection.Credit),
            debit = create(AmountDirection.Debit),
        )
    }
}