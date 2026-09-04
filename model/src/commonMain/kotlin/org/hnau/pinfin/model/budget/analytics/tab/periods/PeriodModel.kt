@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.periods

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDateRange
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.ListScrollState
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.LoadableStateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.model.utils.analytics.AnalyticsConfig
import org.hnau.pinfin.model.utils.analytics.AnalyticsEntry
import org.hnau.pinfin.model.utils.analytics.GroupKey
import org.hnau.pinfin.model.utils.analytics.PeriodResult
import org.hnau.pinfin.model.utils.analytics.calcPeriod

/**
 * Одна страница аналитики - суммы по [groups] за [period] (аналог старого `GraphPageModel`,
 * см. docs/analytics-v2-plan.md, "2.6. Структура кода"). Расчёт делегирован чистой функции
 * [calcPeriod] - никакой собственной логики группировки/деления здесь больше нет.
 */
class PeriodModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val period: LocalDateRange,
    private val totalRange: LocalDateRange,
    private val config: AnalyticsConfig,
    private val groups: List<GroupKey>,
    private val currency: Currency,
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

    val state: StateFlow<Loadable<PeriodResult>> = LoadableStateFlow(
        scope = scope,
    ) {
        calcPeriod(
            entries = dependencies.analyticsEntries,
            period = period,
            totalRange = totalRange,
            config = config,
            groups = groups,
            currency = currency,
        )
    }
}
