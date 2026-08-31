package org.hnau.pinfin.model.utils.analytics.config

import kotlinx.datetime.DatePeriod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.commons.gen.fold.annotations.Fold

@Serializable
data class AnalyticsPageConfig(
    val operation: Operation,
) {

    @Fold
    @Serializable
    sealed interface Operation {
        @Serializable
        @SerialName("sum")
        data object Sum : Operation

        @Serializable
        @SerialName("average")
        data class Average(
            val subperiod: DatePeriod,
        ) : Operation
    }
}