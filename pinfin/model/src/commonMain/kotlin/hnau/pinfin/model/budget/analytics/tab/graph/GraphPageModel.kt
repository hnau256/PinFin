@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budget.analytics.tab.graph

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.model.ListScrollState
import hnau.common.kotlin.KeyValue
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.findMinMax
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.Amount
import hnau.pinfin.model.utils.analytics.AnalyticsEntry
import hnau.pinfin.model.utils.analytics.AnalyticsPage
import hnau.pinfin.model.utils.analytics.config.AnalyticsPageConfig
import hnau.pinfin.model.utils.analytics.splitToPeriods
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDateRange
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class GraphPageModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    page: AnalyticsPage,
    config: AnalyticsPageConfig,
) {

    @Pipe
    interface Dependencies {

        val analyticsEntries: List<AnalyticsEntry>
    }

    @Serializable
    data class Skeleton(
        val scrollState: MutableStateFlow<ListScrollState> =
            ListScrollState.initial.toMutableStateFlowAsInitial(),
    )

    val scrollState: MutableStateFlow<ListScrollState>
        get() = skeleton.scrollState

    private val subperiods: NonEmptyList<LocalDateRange> = page
        .period
        .splitToPeriods(
            duration = config.subPeriod,
            startOfOneOfPeriods = page.period.start,
        )

    data class State(
        val items: NonEmptyList<KeyValue<AnalyticsPage.Item.Key?, Amount>>,
    ) {

        val amountRange: ClosedRange<Amount> = items
            .map(KeyValue<*, Amount>::value)
            .findMinMax()
    }

    val items: StateFlow<Loadable<State?>> = LoadableStateFlow(
        scope = scope,
    ) {
        page
            .items
            .map { item ->
                KeyValue(
                    key = item.key,
                    value = calcItemAmount(
                        operation = config.operation,
                        constraints = item.constraints,
                        entries = dependencies.analyticsEntries,
                    )
                )
            }
            .toNonEmptyListOrNull()
            ?.let(::State)
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