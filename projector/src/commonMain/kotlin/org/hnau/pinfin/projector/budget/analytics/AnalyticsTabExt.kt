package org.hnau.pinfin.projector.budget.analytics

import androidx.compose.runtime.Composable
import org.hnau.pinfin.model.budget.analytics.tab.AnalyticsTab
import org.hnau.pinfin.projector.Localization


@Composable
fun AnalyticsTab.title(
    localization: Localization,
): String = when (this) {
    AnalyticsTab.Accounts -> localization.accounts
    AnalyticsTab.Graph -> localization.categories
}