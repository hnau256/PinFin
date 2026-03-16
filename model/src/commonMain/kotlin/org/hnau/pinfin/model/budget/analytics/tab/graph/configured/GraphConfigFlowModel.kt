package org.hnau.pinfin.model.budget.analytics.tab.graph.configured

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.Delayed
import org.hnau.commons.kotlin.coroutines.flow.state.combineState
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.scopedInState
import org.hnau.commons.kotlin.coroutines.mapStateDelayed
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.map
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.toAccessor
import org.hnau.pinfin.model.utils.analytics.AnalyticsEntry
import org.hnau.pinfin.model.utils.analytics.AnalyticsPagesProvider
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsConfig
import org.hnau.pinfin.model.utils.analytics.toAnalyticsEntries
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo
import org.hnau.pinfin.model.utils.budget.upchain.UpchainHash
import kotlin.time.Clock

class GraphConfigFlowModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    val configStateFlow: StateFlow<AnalyticsConfig>,
    val configure: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        fun config(): GraphConfigModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var config: GraphConfigModel.Skeleton? = null
    )

    val config: StateFlow<GraphConfigModel> = configStateFlow
        .mapWithScope(scope) { scope, config ->
            GraphConfigModel(
                scope = scope,
                skeleton = skeleton::config.toAccessor().getOrInit { GraphConfigModel.Skeleton() },
                dependencies = dependencies.config(),
                config = config,
            )
        }

    val goBackHandler: GoBackHandler =
        config.flatMapState(scope, GraphConfigModel::goBackHandler)
}