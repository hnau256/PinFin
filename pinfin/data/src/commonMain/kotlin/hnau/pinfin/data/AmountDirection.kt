package hnau.pinfin.data

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrThrow
import hnau.common.gen.enumvalues.annotations.EnumValues
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@EnumValues
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