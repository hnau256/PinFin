@file:UseSerializers(
    NonEmptySetSerializer::class,
)

package org.hnau.pinfin.model.utils.analytics.config

import arrow.core.NonEmptySet
import arrow.core.serialization.NonEmptySetSerializer
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId

@Serializable
data class AnalyticsSplitConfig(
    val period: Period,
    val usedAccounts: NonEmptySet<AccountId>?,
    val usedCategories: NonEmptySet<CategoryId?>?,
    val groupBy: GroupBy?,
) {

    enum class GroupBy { Account, Category }

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
        ) : Period
    }
}