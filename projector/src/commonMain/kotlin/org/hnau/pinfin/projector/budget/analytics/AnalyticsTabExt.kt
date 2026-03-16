package org.hnau.pinfin.projector.budget.analytics

import androidx.compose.runtime.Composable
import org.hnau.pinfin.model.budget.analytics.tab.AnalyticsTab
import org.hnau.pinfin.projector.Res
import org.hnau.pinfin.projector.accounts
import org.hnau.pinfin.projector.categories
import org.jetbrains.compose.resources.stringResource

val AnalyticsTab.title: String
    @Composable
    get() = when (this) {
        AnalyticsTab.Accounts -> Res.string.accounts
        AnalyticsTab.Graph -> Res.string.categories
    }.let { res ->
        stringResource(res)
    }