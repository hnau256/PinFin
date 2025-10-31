@file:UseSerializers(
    NonEmptySetSerializer::class,
)

package hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptySet
import arrow.core.serialization.NonEmptySetSerializer
import hnau.pinfin.data.AccountId
import hnau.pinfin.data.CategoryId
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class GraphConfig(
    val period: Period,
    val subPeriod: DatePeriod,
    val view: View,
    val operation: Operation,
    val groupBy: GroupBy?,
    val usedAccounts: NonEmptySet<AccountId>?,
    val usedCategories: NonEmptySet<CategoryId?>?,
) {

    @Serializable
    sealed interface Period {

        @Serializable
        @SerialName("inclusive")
        data object Inclusive : Period

        @Serializable
        @SerialName("fixed")
        data class Fixed(
            val duration: DatePeriod,
            val startOfOneOfPeriods: LocalDate,
            /*val scrollable: Boolean,
            val incremental: Boolean,*/
        ) : Period
    }

    enum class View { Stack, Column, Row }

    enum class GroupBy { Account, Category }

    enum class Operation { Sum, Average }
}