package org.hnau.pinfin.model.budget.analytics.tab.periods

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.Delayed
import org.hnau.commons.kotlin.coroutines.flow.state.combineStateWith
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.mapStateDelayed
import org.hnau.commons.kotlin.ifNull
import org.hnau.commons.kotlin.map
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.model.utils.analytics.AnalyticsConfig
import org.hnau.pinfin.model.utils.analytics.AnalyticsEntry
import org.hnau.pinfin.model.utils.analytics.GroupKey
import org.hnau.pinfin.model.utils.analytics.fold
import org.hnau.pinfin.model.utils.analytics.period.periods
import org.hnau.pinfin.model.utils.analytics.toAnalyticsEntries
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.BudgetState

/**
 * Заменяет старые `GraphConfigModel` + `GraphPagesModel` (см. docs/analytics-v2-plan.md,
 * "2.6. Структура кода"): на каждое изменение [BudgetState] пересчитывает список периодов
 * через [org.hnau.pinfin.model.utils.analytics.period.periods] (без системных часов - только
 * от данных бюджета), затем выбирает текущий период по [selectedPeriodStart] и предоставляет
 * навигацию "назад / вперёд".
 */
class PeriodsModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val config: AnalyticsConfig,
    private val selectedPeriodStart: MutableStateFlow<LocalDate?>,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        fun period(
            analyticsEntries: List<AnalyticsEntry>,
        ): PeriodModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var period: Pair<LocalDate, PeriodModel.Skeleton>? = null,
    )

    val key: String = Json.encodeToString(
        serializer = AnalyticsConfig.serializer(),
        value = config,
    )

    private data class Prepared(
        val entries: List<AnalyticsEntry>,
        val periods: NonEmptyList<LocalDateRange>,
        val totalRange: LocalDateRange,
        val groups: List<GroupKey>,
        val currency: Currency,
    )

    @Fold
    sealed interface State {

        /** Транзакций нет - строить периоды не от чего (docs/analytics-v2-plan.md, "2.1"). */
        data object Empty : State

        data class Data(
            val period: LocalDateRange,
            val model: PeriodModel,
            val switchToPrevious: (() -> Unit)?,
            val switchToNext: (() -> Unit)?,
        ) : State
    }

    private val prepared: StateFlow<Loadable<Delayed<Prepared?>>> = dependencies
        .budgetRepository
        .state
        .mapStateDelayed(scope) { budgetState ->
            prepare(budgetState)
        }

    val state: StateFlow<Loadable<Delayed<State>>> = prepared
        .combineStateWith(
            scope = scope,
            other = selectedPeriodStart,
        ) { preparedLoadable, startOrNull -> preparedLoadable to startOrNull }
        .mapWithScope(scope) { childScope, (preparedLoadable, startOrNull) ->
            preparedLoadable.map { delayed ->
                Delayed(
                    isInProgress = delayed.isInProgress,
                    value = delayed
                        .value
                        ?.let { prepared ->
                            buildState(
                                scope = childScope,
                                prepared = prepared,
                                startOrNull = startOrNull,
                            )
                        }
                        .ifNull { State.Empty },
                )
            }
        }

    private fun prepare(
        state: BudgetState,
    ): Prepared? {
        val transactions = state
            .transactions
            .toNonEmptyListOrNull()
            ?: return null
        val totalRange = transactions.first().value.timestamp..transactions.last().value.timestamp
        val groups: List<GroupKey> = config
            .groupBy
            ?.fold(
                ifAccount = { state.accounts.map(GroupKey::Account) },
                ifCategory = { state.categories.map(GroupKey::Category) + GroupKey.Category(null) },
            )
            .ifNull { listOf(GroupKey.None) }
        return Prepared(
            entries = state
                .transactions
                .flatMap { idWithTransaction ->
                    idWithTransaction.value.toAnalyticsEntries(
                        currency = state.info.currency,
                    )
                },
            periods = config.period.periods(totalRange),
            totalRange = totalRange,
            groups = groups,
            currency = state.info.currency,
        )
    }

    private fun buildState(
        scope: CoroutineScope,
        prepared: Prepared,
        startOrNull: LocalDate?,
    ): State {
        val periods = prepared.periods
        val index = currentIndex(periods, startOrNull)
        val periodRange = periods[index]
        val periodSkeleton = skeleton
            .period
            ?.takeIf { (start, _) -> start == periodRange.start }
            ?.second
            .ifNull {
                PeriodModel.Skeleton().also { newSkeleton ->
                    skeleton.period = periodRange.start to newSkeleton
                }
            }
        val model = PeriodModel(
            scope = scope,
            dependencies = dependencies.period(
                analyticsEntries = prepared.entries.filter { entry -> entry.date in periodRange },
            ),
            skeleton = periodSkeleton,
            period = periodRange,
            totalRange = prepared.totalRange,
            config = config,
            groups = prepared.groups,
            currency = prepared.currency,
        )
        return State.Data(
            period = periodRange,
            model = model,
            switchToPrevious = (index - 1)
                .takeIf { it >= 0 }
                ?.let { previousIndex -> { selectedPeriodStart.value = periods[previousIndex].start } },
            switchToNext = (index + 1)
                .takeIf { it <= periods.lastIndex }
                ?.let { nextIndex -> { selectedPeriodStart.value = periods[nextIndex].start } },
        )
    }

    private fun currentIndex(
        periods: NonEmptyList<LocalDateRange>,
        startOrNull: LocalDate?,
    ): Int {
        if (startOrNull == null) {
            return periods.lastIndex
        }
        val index = periods.indexOfFirst { range -> startOrNull in range }
        return when {
            index >= 0 -> index
            startOrNull < periods.first().start -> 0
            else -> periods.lastIndex
        }
    }

    val goBackHandler: GoBackHandler = selectedPeriodStart.mapState(scope) { startOrNull ->
        startOrNull?.let {
            { selectedPeriodStart.value = null }
        }
    }
}
