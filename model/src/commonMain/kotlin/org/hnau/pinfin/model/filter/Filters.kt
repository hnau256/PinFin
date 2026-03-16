@file:UseSerializers(
    NonEmptySetSerializer::class,
)

package org.hnau.pinfin.model.filter

import arrow.core.NonEmptySet
import arrow.core.serialization.NonEmptySetSerializer
import org.hnau.commons.kotlin.serialization.LocalDateRangeSerializer
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import kotlinx.datetime.LocalDateRange
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class Filters(
    val categories: NonEmptySet<CategoryId?>?,
    val accounts: NonEmptySet<AccountId>?,
    val period: @Serializable(LocalDateRangeSerializer::class) LocalDateRange?,
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