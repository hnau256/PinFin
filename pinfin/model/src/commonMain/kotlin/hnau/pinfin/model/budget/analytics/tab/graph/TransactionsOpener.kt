package hnau.pinfin.model.budget.analytics.tab.graph

import hnau.pinfin.model.filter.Filters

fun interface TransactionsOpener {

    fun openTransactions(
        filters: Filters,
    )
}