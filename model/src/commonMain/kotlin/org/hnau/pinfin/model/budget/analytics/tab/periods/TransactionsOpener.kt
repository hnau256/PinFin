package org.hnau.pinfin.model.budget.analytics.tab.periods

import org.hnau.pinfin.model.filter.Filters

fun interface TransactionsOpener {

    fun openTransactions(
        filters: Filters,
    )
}
