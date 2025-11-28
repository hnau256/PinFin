package hnau.pinfin.model.utils.analytics.config

import kotlinx.datetime.DatePeriod
import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsPageConfig(
    val subPeriod: DatePeriod,
    val operation: Operation,
) {

    enum class Operation { Sum, Average }
}