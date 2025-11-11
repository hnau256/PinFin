package hnau.pinfin.model.utils.analytics.config

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsConfig(
    val split: AnalyticsSplitConfig,
    val page: AnalyticsPageConfig,
    val view: AnalyticsViewConfig,
)