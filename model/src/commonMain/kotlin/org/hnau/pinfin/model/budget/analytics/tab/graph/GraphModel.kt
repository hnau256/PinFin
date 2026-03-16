@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.graph

import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.Delayed
import org.hnau.commons.kotlin.coroutines.flow.state.scopedInState
import org.hnau.commons.kotlin.coroutines.mapStateDelayed
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.map
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
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
import org.hnau.commons.gen.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Clock

class GraphModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
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

    private val pagesProvider = AnalyticsPagesProvider(
        config = config.split,
    )

    val pages: StateFlow<Loadable<Delayed<GraphPagesModel>>> = dependencies
        .budgetRepository
        .state
        .scopedInState(scope)
        .mapStateDelayed(scope) { (scope, state) ->
            val pages = pagesProvider.generatePages(
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

    companion object {

        private val config: AnalyticsConfig = AnalyticsConfig(
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