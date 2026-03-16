package org.hnau.pinfin.model.budget.analytics.tab.graph.configured

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.Delayed
import org.hnau.commons.kotlin.coroutines.flow.state.combineState
import org.hnau.commons.kotlin.coroutines.flow.state.scopedInState
import org.hnau.commons.kotlin.coroutines.mapStateDelayed
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.map
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.toAccessor
import org.hnau.pinfin.model.utils.analytics.AnalyticsEntry
import org.hnau.pinfin.model.utils.analytics.AnalyticsPagesProvider
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsConfig
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsPageConfig
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsSplitConfig
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsViewConfig
import org.hnau.pinfin.model.utils.analytics.toAnalyticsEntries
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo
import org.hnau.pinfin.model.utils.budget.upchain.UpchainHash
import kotlin.time.Clock

class GraphConfiguredModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    val config: StateFlow<AnalyticsConfig>,
    val configure: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        fun pages(
            analyticsEntries: List<AnalyticsEntry>,
        ): GraphPagesModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var pages: Pair<UpchainHash?, GraphPagesModel.Skeleton>? = null,
    )

    val pages: StateFlow<Loadable<Delayed<GraphPagesModel>>> = combineState(
        scope = scope,
        first = dependencies.budgetRepository.state,
        second = config,
    ) { state, config ->
        state to config
    }
        .scopedInState(scope)
        .mapStateDelayed(scope) { (scope, stateWithConfig) ->
            val (state, config) = stateWithConfig
            val pages = AnalyticsPagesProvider(
                config = config.split,
            ).generatePages(
                state = state,
                today = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            )
            GraphPagesModel(
                scope = scope,
                dependencies = dependencies.pages(
                    analyticsEntries = state
                        .transactions
                        .flatMap(TransactionInfo::toAnalyticsEntries),
                ),
                skeleton = skeleton::pages
                    .toAccessor()
                    .map(
                        Mapper(
                            direct = { hashWithPagesSkeletonOrNull ->
                                hashWithPagesSkeletonOrNull?.let { (hash, pagesSkeleton) ->
                                    pagesSkeleton.takeIf { hash == state.hash }
                                }
                            },
                            reverse = { skeletonOrNull ->
                                skeletonOrNull?.let { skeleton -> state.hash to skeleton }
                            }
                        )
                    )
                    .getOrInit { GraphPagesModel.Skeleton() },
                pages = pages,
                pageConfig = config.page,
            )
        }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}