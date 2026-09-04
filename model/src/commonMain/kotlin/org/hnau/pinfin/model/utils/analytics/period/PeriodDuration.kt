package org.hnau.pinfin.model.utils.analytics.period

import kotlinx.serialization.Serializable

/** Длительность без якоря — для подпериода в «Среднем» (подпериоды выравниваются по началу периода страницы). */
@Serializable
data class PeriodDuration(
    val count: Int,        // ≥ 1
    val unit: PeriodUnit,  // Day | Month | Year
)
