package org.hnau.pinfin.data

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrThrow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.commons.gen.enumvalues.annotations.EnumValues

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