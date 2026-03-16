@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.graph

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.GraphConfigureModel
import org.hnau.pinfin.model.budget.analytics.tab.graph.configured.GraphConfiguredModel
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsConfig
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsPageConfig
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsSplitConfig
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsViewConfig
import kotlin.time.Clock

class GraphModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @SealUp(
        variants = [
            Variant(
                type = GraphConfiguredModel::class,
                identifier = "configured",
            ),
            Variant(
                type = GraphConfigureModel::class,
                identifier = "configure",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "GraphStateModel",
    )
    interface State {

        val goBackHandler: GoBackHandler

        companion object
    }

    @SealUp(
        variants = [
            Variant(
                type = GraphConfiguredModel.Skeleton::class,
                identifier = "configured",
            ),
            Variant(
                type = GraphConfigureModel.Skeleton::class,
                identifier = "configure",
            ),
        ],
        wrappedValuePropertyName = "skeleton",
        sealedInterfaceName = "GraphStateSkeleton",
        serializable = true,
    )
    interface StateSkeleton {

        companion object
    }

    @Pipe
    interface Dependencies {

        @Pipe
        interface WithConfig {

            fun configured(): GraphConfiguredModel.Dependencies

            fun configure(): GraphConfigureModel.Dependencies
        }

        fun withConfig(
            config: StateFlow<AnalyticsConfig>,
        ): WithConfig
    }

    @Serializable
    data class Skeleton(
        val config: MutableStateFlow<AnalyticsConfig> =
            defaultConfig.toMutableStateFlowAsInitial(),

        val state: MutableStateFlow<GraphStateSkeleton> =
            StateSkeleton.configured().toMutableStateFlowAsInitial(),
    )

    private val dependenciesWithConfig = dependencies
        .withConfig(skeleton.config)

    val state: StateFlow<GraphStateModel> = skeleton
        .state
        .mapWithScope(scope) { scope, state ->
            state.fold(
                ifConfigured = { configured ->
                    State.configured(
                        scope = scope,
                        skeleton = configured,
                        dependencies = dependenciesWithConfig.configured(),
                        config = {
                            updateState(
                                StateSkeleton.configure()
                            )
                        }
                    )
                },
                ifConfigure = { configure ->
                    State.configure(
                        scope = scope,
                        skeleton = configure,
                        dependencies = dependenciesWithConfig.configure(),
                        updateConfig = skeleton.config::value::set,
                    )
                },
            )
        }

    private fun updateState(
        newState: GraphStateSkeleton,
    ) {
        skeleton.state.value = newState
    }

    val goBackHandler: GoBackHandler = state
        .flatMapState(
            scope = scope,
            transform = GraphStateModel::goBackHandler,
        )
        .flatMapState(scope) { goBackOrNull: (() -> Unit)? ->
            goBackOrNull.foldNullable(
                ifNotNull = { it.toMutableStateFlowAsInitial() },
                ifNull = {
                    skeleton
                        .state
                        .mapState(scope) { state ->
                            state.fold(
                                ifConfigured = { null },
                                ifConfigure = {
                                    {
                                        updateState(
                                            StateSkeleton.configured(),
                                        )
                                    }
                                }
                            )
                        }
                },
            )
        }

    companion object {

        private val defaultConfig: AnalyticsConfig = AnalyticsConfig(
            split = AnalyticsSplitConfig(
                period = AnalyticsSplitConfig.Period.Fixed(
                    duration = DatePeriod(months = 1),
                    startOfOneOfPeriods = Clock.System
                        .now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                        .let { today ->
                            LocalDate(
                                year = today.year,
                                month = today.month,
                                day = 1,
                            )
                        },
                    incremental = false,
                ),
                usedAccounts = null,
                usedCategories = null,
                groupBy = AnalyticsSplitConfig.GroupBy.Category,
            ),
            page = AnalyticsPageConfig(
                subPeriod = DatePeriod(
                    days = 1,
                ),
                operation = AnalyticsPageConfig.Operation.Sum,
            ),
            view = AnalyticsViewConfig(
                view = AnalyticsViewConfig.View.Column,
            ),
        )
    }
}