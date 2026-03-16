@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.graph.configured

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import arrow.core.toNonEmptyListOrThrow
import org.hnau.commons.app.model.ListScrollState
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.LoadableStateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.groupByToNonEmpty
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.AmountDirectionValues
import org.hnau.pinfin.data.sum
import org.hnau.pinfin.model.filter.Filters
import org.hnau.pinfin.model.utils.analytics.AnalyticsEntry
import org.hnau.pinfin.model.utils.analytics.AnalyticsPage
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsPageConfig
import org.hnau.pinfin.model.utils.analytics.splitToPeriods
import org.hnau.commons.gen.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDateRange
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.pinfin.model.budget.analytics.tab.graph.TransactionsOpener

class GraphPageModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val page: AnalyticsPage,
    config: AnalyticsPageConfig,
) {

    @Pipe
    interface Dependencies {

        val transactionsOpener: TransactionsOpener

        val analyticsEntries: List<AnalyticsEntry>
    }

    @Serializable
    data class Skeleton(
        val scrollState: MutableStateFlow<ListScrollState> =
            ListScrollState.initial.toMutableStateFlowAsInitial(),
    )

    val scrollState: MutableStateFlow<ListScrollState>
        get() = skeleton.scrollState

    val transactionsOpener: TransactionsOpener
        get() = dependencies.transactionsOpener

    val period: LocalDateRange
        get() = page.period

    private val subperiods: NonEmptyList<LocalDateRange> = page
        .period
        .splitToPeriods(
            duration = config.subPeriod,
            startOfOneOfPeriods = page.period.start,
            incremental = false,
        )

    data class State(
        val values: AmountDirectionValues<Half?>,
    ) {

        data class Half(
            val values: NonEmptyList<KeyValue<AnalyticsPage.Item.Key?, Value>>,
        ) {

            data class Value(
                val amount: Amount,
                val filters: Filters,
            )

            private val amounts: NonEmptyList<Amount> = values
                .map { item -> item.value.amount }

            val max: Amount = amounts.max()

            val sum: Amount = amounts.sum()
        }

        val total: Amount? = AmountDirection
            .entries
            .mapNotNull { direction ->
                values[direction]
                    ?.sum
                    ?.withDirection(direction)
            }
            .takeIf { it.size > 1 }
            ?.sum()
    }

    val state: StateFlow<Loadable<State?>> = LoadableStateFlow(
        scope = scope,
    ) {
        page
            .items
            .mapNotNull { item ->
                val amount = calcItemAmount(
                    operation = config.operation,
                    constraints = item.constraints,
                    entries = dependencies.analyticsEntries,
                )
                    .takeIf { it != Amount.zero }
                    ?: return@mapNotNull null

                KeyValue(
                    key = item.key,
                    value = State.Half.Value(
                        amount = amount,
                        filters = Filters(
                            categories = item.constraints.categories,
                            accounts = item.constraints.accounts,
                            period = page.period,
                        ),
                    ),
                )
            }
            .groupByToNonEmpty { (key, value) ->
                value
                    .amount
                    .splitToDirectionAndRaw()
                    .map { positiveAmount ->
                        KeyValue(
                            key = key,
                            value = State.Half.Value(
                                amount = positiveAmount,
                                filters = value.filters,
                            ),
                        )
                    }
            }
            .mapValues { (_, values) ->
                values
                    .sortedByDescending { item -> item.value.amount }
                    .toNonEmptyListOrThrow()
            }
            .let { valuesByDirection ->
                State(
                    AmountDirectionValues.create { direction ->
                        val values = valuesByDirection[direction] ?: return@create null
                        State.Half(values)
                    }
                )
            }
    }

    private fun calcItemAmount(
        operation: AnalyticsPageConfig.Operation,
        constraints: AnalyticsPage.Item.Constraints,
        entries: List<AnalyticsEntry>,
    ): Amount = subperiods
        .map { subperiod ->
            calcSubperiodAmount(
                subperiod = subperiod,
                constraints = constraints,
                entries = entries,
            )
        }
        .let { subperiodsAmounts ->
            when (operation) {
                AnalyticsPageConfig.Operation.Sum -> subperiodsAmounts
                    .sum

                AnalyticsPageConfig.Operation.Average -> subperiodsAmounts
                    .sum
                    .value
                    .toDouble()
                    .div(subperiodsAmounts.size)
                    .toInt()
                    .let(::Amount)
            }
        }

    private val NonEmptyList<Amount>.sum: Amount
        get() = tail.fold(
            initial = head,
            operation = Amount::plus,
        )

    private fun calcSubperiodAmount(
        subperiod: LocalDateRange,
        constraints: AnalyticsPage.Item.Constraints,
        entries: List<AnalyticsEntry>,
    ): Amount = entries
        .filter { entry -> entry.date in subperiod }
        .filter { entry -> entry.matches(constraints) }
        .toNonEmptyListOrNull()
        .foldNullable(
            ifNull = { Amount.zero },
            ifNotNull = { entries ->
                entries
                    .map(AnalyticsEntry::amount)
                    .sum
            }
        )

    @Suppress("RedundantIf")
    private fun AnalyticsEntry.matches(
        constraints: AnalyticsPage.Item.Constraints,
    ): Boolean {

        val categories = constraints.categories
        if (categories != null && category?.id !in categories) {
            return false
        }

        val accounts = constraints.accounts
        if (accounts != null && account.id !in accounts) {
            return false
        }
        return true
    }
}