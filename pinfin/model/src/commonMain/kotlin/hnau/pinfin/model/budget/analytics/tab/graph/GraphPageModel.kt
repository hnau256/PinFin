@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budget.analytics.tab.graph

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import arrow.core.toNonEmptyListOrThrow
import hnau.common.app.model.ListScrollState
import hnau.common.kotlin.KeyValue
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.groupByToNonEmpty
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.Amount
import hnau.pinfin.data.AmountDirection
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
    private val page: AnalyticsPage,
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

    val period: LocalDateRange
        get() = page.period

    private val subperiods: NonEmptyList<LocalDateRange> = page
        .period
        .splitToPeriods(
            duration = config.subPeriod,
            startOfOneOfPeriods = page.period.start,
        )

    sealed interface State {

        data class CreditOnly(
            val credit: Half,
        ) : State

        data class DebitOnly(
            val debit: Half,
        ) : State

        data class CreditAndDebit(
            val credit: Half,
            val debit: Half,
        ) : State

        data class Half(
            val values: NonEmptyList<KeyValue<AnalyticsPage.Item.Key?, Amount>>,
        ) {

            val max: Amount = values
                .map(KeyValue<*, Amount>::value)
                .max()
        }
    }

    val state: StateFlow<Loadable<State?>> = LoadableStateFlow(
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
            .groupByToNonEmpty { (key, amount) ->
                amount
                    .splitToDirectionAndRaw()
                    .map { positiveAmount ->
                        KeyValue(
                            key = key,
                            value = positiveAmount,
                        )
                    }
            }
            .mapValues { (_, values) ->
                values
                    .sortedByDescending(KeyValue<*, Amount>::value)
                    .toNonEmptyListOrThrow()
            }
            .let { valuesByDirection ->
                val creditOrNull = valuesByDirection[AmountDirection.Credit]
                val debitOrNull = valuesByDirection[AmountDirection.Debit]
                creditOrNull.foldNullable(
                    ifNull = {
                        debitOrNull?.let { debit ->
                            State.DebitOnly(State.Half(debit))
                        }
                    },
                    ifNotNull = { credit ->
                        debitOrNull.foldNullable(
                            ifNull = { State.CreditOnly(State.Half(credit)) },
                            ifNotNull = { debit ->
                                State.CreditAndDebit(
                                    credit = State.Half(credit),
                                    debit = State.Half(debit),
                                )
                            }
                        )
                    },
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