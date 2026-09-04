package org.hnau.pinfin.model.budget.analytics.tab.periods

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.toAccessor
import org.hnau.pinfin.model.utils.analytics.AnalyticsConfig

/**
 * Аналог старого `GraphConfigFlowModel`: на каждое изменение конфига пересоздаёт [PeriodsModel]
 * (см. docs/analytics-v2-plan.md, "2.6. Структура кода").
 */
class PeriodsFlowModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    configStateFlow: StateFlow<AnalyticsConfig>,
    selectedPeriodStart: MutableStateFlow<LocalDate?>,
    val configure: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        fun periods(): PeriodsModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var periods: PeriodsModel.Skeleton? = null,
    )

    val periods: StateFlow<PeriodsModel> = configStateFlow
        .mapWithScope(scope) { scope, config ->
            PeriodsModel(
                scope = scope,
                dependencies = dependencies.periods(),
                skeleton = skeleton::periods.toAccessor().getOrInit { PeriodsModel.Skeleton() },
                config = config,
                selectedPeriodStart = selectedPeriodStart,
            )
        }

    val goBackHandler: GoBackHandler =
        periods.flatMapState(scope, PeriodsModel::goBackHandler)
}
