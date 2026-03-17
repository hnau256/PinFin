@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.split

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsSplitConfig
import kotlin.time.Clock

class ConfigSplitPeriodModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
) {

    enum class Tab { Inclusive, Fixed }

    @Serializable
    data class Skeleton(
        val initialTab: Tab,
        var fixed: ConfigPeriodModel.Skeleton?,
        val tab: MutableStateFlow<Tab> = initialTab.toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun create(
                initial: AnalyticsSplitConfig.Period,
            ): Skeleton = when (initial) {
                AnalyticsSplitConfig.Period.Inclusive -> Skeleton(
                    initialTab = Tab.Inclusive,
                    fixed = null,
                )

                is AnalyticsSplitConfig.Period.Fixed -> Skeleton(
                    initialTab = Tab.Fixed,
                    fixed = ConfigPeriodModel.Skeleton.create(
                        initial = initial.duration,
                    ),
                )
            }
        }
    }

    val tab: MutableStateFlow<Tab>
        get() = skeleton.tab

    val state: StateFlow<ConfigSplitPeriodModelState<ConfigPeriodModel>> = skeleton
        .tab
        .mapWithScope(scope) { scope, tab ->
            when (tab) {
                Tab.Inclusive -> ConfigSplitPeriodModelState.Inclusive
                Tab.Fixed -> ConfigSplitPeriodModelState.Fixed(
                    period = ConfigPeriodModel(
                        scope = scope,
                        skeleton = skeleton::fixed
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

    internal val editablePeriod: StateFlow<Editable<AnalyticsSplitConfig.Period>> = state
        .flatMapWithScope(scope) { scope, state ->
            val tabChanged = state.tab != skeleton.initialTab
            state.fold(
                ifInclusive = {
                    Editable.Value(
                        changed = tabChanged,
                        value = AnalyticsSplitConfig.Period.Inclusive
                    ).toMutableStateFlowAsInitial()
                },
                ifFixed = { period ->
                    period
                        .periodEditable
                        .mapState(scope) { periodEditable ->
                            periodEditable.flatMap { period ->
                                Editable.Value(
                                    value = AnalyticsSplitConfig.Period.Fixed(
                                        duration = period,
                                        startOfOneOfPeriods = Clock.System
                                            .now()
                                            .toLocalDateTime(TimeZone.currentSystemDefault())
                                            .date
                                            .let { today ->
                                                LocalDate(
                                                    year = today.year,
                                                    month = Month.JANUARY,
                                                    day = 1,
                                                )
                                            },
                                    ),
                                    changed = tabChanged,
                                )
                            }
                        }
                }
            )
        }

}

