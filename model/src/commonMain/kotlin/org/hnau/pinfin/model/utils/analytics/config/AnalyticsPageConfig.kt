package org.hnau.pinfin.model.utils.analytics.config

import kotlinx.datetime.DatePeriod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsPageConfig(
    val operation: Operation,
) {

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