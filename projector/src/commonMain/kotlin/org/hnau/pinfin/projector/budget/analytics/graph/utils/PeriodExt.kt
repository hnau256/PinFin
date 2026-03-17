package org.hnau.pinfin.projector.budget.analytics.graph.utils

import arrow.core.toNonEmptyListOrNull
import kotlinx.datetime.DatePeriod
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsSplitConfig
import org.hnau.pinfin.projector.Localization

internal fun DatePeriod.format(
    localization: Localization,
): String = listOf(
    years to localization.year,
    months to localization.month,
    days to localization.day,
)
    .mapNotNull { (countOrZero, unit) ->
        val count = countOrZero
            .takeIf { it != 0 }
            ?: return@mapNotNull null
        val suffix = count
            .takeIf { it > 1 }
            ?.let { positiveCount -> " x$positiveCount" }
            ?: ""
        unit + suffix
    }
    .toNonEmptyListOrNull()
    ?.joinToString(
        separator = " + ",
    )
    ?: "0"

internal fun AnalyticsSplitConfig.Period.format(
    localization: Localization,
): String = when (this) {
    is AnalyticsSplitConfig.Period.Fixed -> duration.format(
        localization = localization,
    )

    AnalyticsSplitConfig.Period.Inclusive -> localization
        .inclusivePeriod
        .replaceFirstChar(Char::lowercase)
}