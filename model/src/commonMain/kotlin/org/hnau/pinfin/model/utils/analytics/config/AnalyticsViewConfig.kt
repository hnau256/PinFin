package org.hnau.pinfin.model.utils.analytics.config

import kotlinx.serialization.Serializable
import org.hnau.commons.gen.fold.annotations.Fold

@Serializable
data class AnalyticsViewConfig(
    val view: View,
    //val scrollable: Boolean,
) {

    @Fold
    enum class View { Stack, Column, Row }
}