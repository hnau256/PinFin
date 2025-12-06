package hnau.pinfin.model.budget.analytics.tab


@Deprecated("Use from EnumValues")
data class AnalyticsTabValues<out T>(
    val accounts: T,
    val graph: T,
) {

    operator fun get(
        amountDirection: AnalyticsTab,
    ): T = when (amountDirection) {
        AnalyticsTab.Accounts -> accounts
        AnalyticsTab.Graph -> graph
    }

    inline fun <R> map(
        transform: (amountDirection: AnalyticsTab, value: T) -> R,
    ): AnalyticsTabValues<R> = AnalyticsTabValues(
        accounts = transform(AnalyticsTab.Accounts, accounts),
        graph = transform(AnalyticsTab.Graph, graph),
    )

    inline fun <R> map(
        transform: (value: T) -> R,
    ): AnalyticsTabValues<R> = map { _, value ->
        transform(value)
    }

    inline fun <O, R> combineWith(
        other: AnalyticsTabValues<O>,
        combine: (amountDirection: AnalyticsTab, value: T, other: O) -> R,
    ): AnalyticsTabValues<R> = AnalyticsTabValues(
        accounts = combine(AnalyticsTab.Accounts, accounts, other.accounts),
        graph = combine(AnalyticsTab.Graph, graph, other.graph),
    )

    inline fun <O, R> combineWith(
        other: AnalyticsTabValues<O>,
        combine: (value: T, other: O) -> R,
    ): AnalyticsTabValues<R> = combineWith(
        other = other,
    ) { _, value, other ->
        combine(value, other)
    }

    companion object {

        inline fun <T> create(
            createValue: (amountDirection: AnalyticsTab) -> T,
        ): AnalyticsTabValues<T> = AnalyticsTabValues(
            accounts = createValue(AnalyticsTab.Accounts),
            graph = createValue(AnalyticsTab.Graph),
        )
    }
}