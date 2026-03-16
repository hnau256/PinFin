package org.hnau.pinfin.model.budget.analytics.tab.graph.configured

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.toAccessor
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsConfig

class GraphConfigFlowModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    configStateFlow: StateFlow<AnalyticsConfig>,
    private val configure: () -> Unit,
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
                configure = configure,
            )
        }

    val goBackHandler: GoBackHandler =
        config.flatMapState(scope, GraphConfigModel::goBackHandler)
}