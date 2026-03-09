package hnau.pinfin.projector.budget.analytics

import androidx.compose.runtime.Composable
import hnau.pinfin.model.budget.analytics.tab.AnalyticsTab
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.accounts
import hnau.pinfin.projector.categories
import org.jetbrains.compose.resources.stringResource

val AnalyticsTab.title: String
    @Composable
    get() = when (this) {
        AnalyticsTab.Accounts -> Res.string.accounts
        AnalyticsTab.Graph -> Res.string.categories
    }.let { res ->
        stringResource(res)
    }