@file:UseSerializers(
    NonEmptySetSerializer::class,
)

package hnau.pinfin.model.filter

import arrow.core.NonEmptySet
import arrow.core.serialization.NonEmptySetSerializer
import hnau.pinfin.data.AccountId
import hnau.pinfin.data.CategoryId
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class Filters(
    val categories: NonEmptySet<CategoryId?>?,
    val accounts: NonEmptySet<AccountId>?,
    val period: ClosedRange<LocalDate>?,
) {

    val any: Boolean
        get() = categories != null ||
                accounts != null ||
                period != null



    companion object {

        val empty = Filters(
            categories = null,
            accounts = null,
            period = null,
        )
    }
}