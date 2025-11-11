@file:UseSerializers(
    NonEmptySetSerializer::class,
)

package hnau.pinfin.model.utils.analytics.config

import arrow.core.NonEmptySet
import arrow.core.serialization.NonEmptySetSerializer
import hnau.pinfin.data.AccountId
import hnau.pinfin.data.CategoryId
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class AnalyticsItemConstraints(
    val categories: NonEmptySet<CategoryId?>?,
    val accounts: NonEmptySet<AccountId>?,
) {

    fun toFilters(
        period: AnalyticsSplitConfig.Period,
    )
}