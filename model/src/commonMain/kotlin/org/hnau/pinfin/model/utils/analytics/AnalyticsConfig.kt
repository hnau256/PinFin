@file:UseSerializers(
    NonEmptySetSerializer::class,
)

package org.hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptySet
import arrow.core.serialization.NonEmptySetSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.model.utils.analytics.period.AnalyticsPeriod
import org.hnau.pinfin.model.utils.analytics.period.PeriodDuration

@Serializable
data class AnalyticsConfig(
    val period: AnalyticsPeriod,
    val groupBy: GroupBy? = GroupBy.Category,
    val operation: Operation = Operation.Sum,
    val accounts: NonEmptySet<AccountId>? = null,
    val categories: NonEmptySet<CategoryId?>? = null,
) {

    @Fold
    enum class GroupBy { Account, Category }

    @Fold
    @Serializable
    sealed interface Operation {

        @Serializable
        @SerialName("sum")
        data object Sum : Operation

        @Serializable
        @SerialName("average")
        data class Average(
            val subperiod: PeriodDuration,
        ) : Operation
    }
}
