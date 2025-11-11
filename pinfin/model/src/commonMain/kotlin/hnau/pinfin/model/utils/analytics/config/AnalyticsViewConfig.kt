package hnau.pinfin.model.utils.analytics.config

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsViewConfig(
    val view: View,
    //val scrollable: Boolean,
) {

    enum class View { Stack, Column, Row }
}