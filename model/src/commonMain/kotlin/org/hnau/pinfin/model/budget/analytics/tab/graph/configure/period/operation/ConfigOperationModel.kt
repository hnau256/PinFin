@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.operation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DatePeriod
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.commons.kotlin.toAccessor
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.ConfigPeriodModel
import org.hnau.pinfin.model.transaction.utils.Editable
import org.hnau.pinfin.model.transaction.utils.flatMap
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsPageConfig

class ConfigOperationModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
) {

    enum class Tab { Sum, Average }

    @Serializable
    data class Skeleton(
        val initialTab: Tab,
        var averageSubperiod: ConfigPeriodModel.Skeleton?,
        val tab: MutableStateFlow<Tab> = initialTab.toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun create(
                initial: AnalyticsPageConfig.Operation,
            ): Skeleton = when (initial) {
                AnalyticsPageConfig.Operation.Sum -> Skeleton(
                    initialTab = Tab.Sum,
                    averageSubperiod = null,
                )

                is AnalyticsPageConfig.Operation.Average -> Skeleton(
                    initialTab = Tab.Average,
                    averageSubperiod = ConfigPeriodModel.Skeleton.create(
                        initial = initial.subperiod,
                    ),
                )
            }
        }
    }

    val tab: MutableStateFlow<Tab>
        get() = skeleton.tab

    val state: StateFlow<ConfigOperationModelState<ConfigPeriodModel>> = skeleton
        .tab
        .mapWithScope(scope) { scope, tab ->
            when (tab) {
                Tab.Sum -> ConfigOperationModelState.Sum
                Tab.Average -> ConfigOperationModelState.Average(
                    subperiod = ConfigPeriodModel(
                        scope = scope,
                        skeleton = skeleton::averageSubperiod
                            .toAccessor()
                            .getOrInit {
                                ConfigPeriodModel.Skeleton.create(
                                    DatePeriod(
                                        months = 1,
                                    )
                                )
                            }
                    )
                )
            }
        }

    internal val editableOperation: StateFlow<Editable<AnalyticsPageConfig.Operation>> = state
        .flatMapWithScope(scope) { scope, state ->
            val tabChanged = state.tab != skeleton.initialTab
            state.fold(
                ifSum = {
                    Editable.Value(
                        changed = tabChanged,
                        value = AnalyticsPageConfig.Operation.Sum
                    ).toMutableStateFlowAsInitial()
                },
                ifAverage = { subperiod ->
                    subperiod
                        .periodEditable
                        .mapState(scope) { periodEditable ->
                            periodEditable.flatMap { period ->
                                Editable.Value(
                                    value = AnalyticsPageConfig.Operation.Average(
                                        subperiod = period,
                                    ),
                                    changed = tabChanged,
                                )
                            }
                        }
                }
            )
        }
}

