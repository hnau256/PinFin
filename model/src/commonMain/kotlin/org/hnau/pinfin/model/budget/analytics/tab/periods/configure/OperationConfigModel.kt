@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.periods.configure

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.flatMap
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.commons.kotlin.toAccessor
import org.hnau.pinfin.model.budget.analytics.tab.periods.configure.period.DurationModel
import org.hnau.pinfin.model.utils.analytics.AnalyticsConfig
import org.hnau.pinfin.model.utils.analytics.period.PeriodDuration
import org.hnau.pinfin.model.utils.analytics.period.PeriodUnit
import org.hnau.pinfin.model.utils.analytics.fold

/**
 * Сумма / среднее (docs/analytics-v2-plan.md, "2.3", "2.6"). Порт старого
 * `ConfigOperationModel`, но подпериод «Среднего» - тоже [DurationModel] (одна единица,
 * без Y/M/D одновременно - см. "Решения автора", п. 7).
 */
class OperationConfigModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
) {

    @Fold
    enum class Tab { Sum, Average }

    @Serializable
    data class Skeleton(
        val initialTab: Tab,
        var averageSubperiod: DurationModel.Skeleton?,
        val tab: MutableStateFlow<Tab> = initialTab.toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun create(
                initial: AnalyticsConfig.Operation,
            ): Skeleton = initial.fold(
                ifSum = {
                    Skeleton(
                        initialTab = Tab.Sum,
                        averageSubperiod = null,
                    )
                },
                ifAverage = { subperiod ->
                    Skeleton(
                        initialTab = Tab.Average,
                        averageSubperiod = DurationModel.Skeleton.create(
                            initial = subperiod,
                        ),
                    )
                },
            )
        }
    }

    val tab: MutableStateFlow<Tab>
        get() = skeleton.tab

    val state: StateFlow<OperationConfigModelState<DurationModel>> = skeleton
        .tab
        .mapWithScope(scope) { scope, tab ->
            tab.fold(
                ifSum = {
                    OperationConfigModelState.Sum
                },
                ifAverage = {
                    OperationConfigModelState.Average(
                        subperiod = DurationModel(
                            scope = scope,
                            skeleton = skeleton::averageSubperiod
                                .toAccessor()
                                .getOrInit {
                                    DurationModel.Skeleton.create(
                                        PeriodDuration(
                                            count = 1,
                                            unit = PeriodUnit.Month,
                                        )
                                    )
                                }
                        )
                    )
                },
            )
        }

    internal val editableOperation: StateFlow<Editable<AnalyticsConfig.Operation>> = state
        .flatMapWithScope(scope) { scope, state ->
            val tabChanged = state.tab != skeleton.initialTab
            state.fold(
                ifSum = {
                    Editable.Value(
                        changed = tabChanged,
                        value = AnalyticsConfig.Operation.Sum,
                    ).toMutableStateFlowAsInitial()
                },
                ifAverage = { subperiod ->
                    subperiod
                        .duration
                        .mapState(scope) { durationEditable ->
                            durationEditable.flatMap { duration ->
                                Editable.Value(
                                    value = AnalyticsConfig.Operation.Average(
                                        subperiod = duration,
                                    ),
                                    changed = tabChanged,
                                )
                            }
                        }
                }
            )
        }
}
