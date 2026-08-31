package org.hnau.pinfin.projector.budget.analytics

import androidx.compose.runtime.Composable
import org.hnau.pinfin.model.budget.analytics.tab.AnalyticsTab
import org.hnau.pinfin.model.budget.analytics.tab.fold
import org.hnau.pinfin.projector.Localization


@Composable
fun AnalyticsTab.title(
    localization: Localization,
): String = fold(
    ifAccounts = { localization.accounts },
    ifGraph = { localization.categories },
)