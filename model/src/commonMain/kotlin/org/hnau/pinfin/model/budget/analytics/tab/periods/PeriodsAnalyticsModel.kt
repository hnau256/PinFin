@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.periods

import arrow.core.toNonEmptyListOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.budget.analytics.tab.periods.configure.AnalyticsConfigureModel
import org.hnau.pinfin.model.utils.analytics.AnalyticsConfig
import org.hnau.pinfin.model.utils.analytics.period.AnalyticsPeriod
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.BudgetState

/**
 * Заменяет старый `GraphModel` (см. docs/analytics-v2-plan.md, "2.6. Структура кода"): хранит
 * конфиг аналитики и выбранный период, переключает между просмотром ([PeriodsFlowModel]) и
 * экраном настроек ([AnalyticsConfigureModel] - пока заглушка, полноценный экран будет в фазе 3).
 */
class PeriodsAnalyticsModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @SealUp(
        variants = [
            Variant(
                type = PeriodsFlowModel::class,
                identifier = "configured",
            ),
            Variant(
                type = AnalyticsConfigureModel::class,
                identifier = "configure",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "PeriodsStateModel",
    )
    interface State {

        val goBackHandler: GoBackHandler

        companion object
    }

    @SealUp(
        variants = [
            Variant(
                type = PeriodsFlowModel.Skeleton::class,
                identifier = "configured",
            ),
            Variant(
                type = AnalyticsConfigureModel.Skeleton::class,
                identifier = "configure",
            ),
        ],
        wrappedValuePropertyName = "skeleton",
        sealedInterfaceName = "PeriodsStateSkeleton",
        serializable = true,
    )
    interface StateSkeleton {

        companion object
    }

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        fun configured(): PeriodsFlowModel.Dependencies

        fun configure(): AnalyticsConfigureModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val config: MutableStateFlow<AnalyticsConfig> =
            defaultConfig.toMutableStateFlowAsInitial(),

        val selectedPeriodStart: MutableStateFlow<LocalDate?> =
            null.toMutableStateFlowAsInitial(),

        val state: MutableStateFlow<PeriodsStateSkeleton> =
            StateSkeleton.configured().toMutableStateFlowAsInitial(),
    )

    val state: StateFlow<PeriodsStateModel> = skeleton
        .state
        .mapWithScope(scope) { scope, state ->
            state.fold(
                ifConfigured = { configured ->
                    State.configured(
                        scope = scope,
                        skeleton = configured,
                        dependencies = dependencies.configured(),
                        configStateFlow = skeleton.config,
                        selectedPeriodStart = skeleton.selectedPeriodStart,
                        configure = {
                            val (firstTransactionDate, lastTransactionDate) = transactionDateBounds()
                            updateState(
                                StateSkeleton.configure(
                                    configure = AnalyticsConfigureModel.Skeleton.create(
                                        initial = skeleton.config.value,
                                        firstTransactionDate = firstTransactionDate,
                                        lastTransactionDate = lastTransactionDate,
                                    )
                                )
                            )
                        },
                    )
                },
                ifConfigure = { configure ->
                    val (firstTransactionDate, lastTransactionDate) = transactionDateBounds()
                    State.configure(
                        scope = scope,
                        dependencies = dependencies.configure(),
                        skeleton = configure,
                        firstTransactionDate = firstTransactionDate,
                        lastTransactionDate = lastTransactionDate,
                        onReady = { newConfig ->
                            skeleton.config.value = newConfig
                            switchToConfigured()
                        },
                        onCancel = ::switchToConfigured,
                    )
                },
            )
        }

    private fun switchToConfigured() {
        updateState(
            StateSkeleton.configured(),
        )
    }

    private fun updateState(
        newState: PeriodsStateSkeleton,
    ) {
        skeleton.state.value = newState
    }

    val goBackHandler: GoBackHandler = derivedStateFlowOf(scope) {
        state.state.goBackHandler.state
            ?: skeleton.state.state.fold(
                ifConfigured = { null },
                ifConfigure = { ::switchToConfigured },
            )
    }

    /**
     * Диапазон дат транзакций - нужен только как затравка для дефолтов якоря в
     * [AnalyticsConfigureModel] (docs/analytics-v2-plan.md, "2.2"). Если транзакций нет,
     * подставляется детерминированная заглушка - часы не используются нигде в аналитике.
     */
    private fun transactionDateBounds(): Pair<LocalDate, LocalDate> {
        val transactions = dependencies
            .budgetRepository
            .state
            .value
            .transactions
            .toNonEmptyListOrNull()
            ?: return fallbackDate to fallbackDate
        return transactions.first().value.timestamp to transactions.last().value.timestamp
    }

    companion object {

        private val fallbackDate: LocalDate = LocalDate(1970, 1, 1)

        val defaultConfig: AnalyticsConfig = AnalyticsConfig(
            period = AnalyticsPeriod.Months(
                count = 1,
                startDay = 1,
            ),
        )
    }
}
