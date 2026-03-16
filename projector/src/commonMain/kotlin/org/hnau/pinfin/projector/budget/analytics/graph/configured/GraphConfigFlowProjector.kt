package org.hnau.pinfin.projector.budget.analytics.graph.configured

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.state.TransitionSpec
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.map
import org.hnau.pinfin.model.budget.analytics.tab.graph.configured.GraphConfigFlowModel

class GraphConfigFlowProjector(
    scope: CoroutineScope,
    model: GraphConfigFlowModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun config(): GraphConfigProjector.Dependencies
    }

    private val config: StateFlow<GraphConfigProjector> = model
        .config
        .mapWithScope(scope) { scope, config ->
            GraphConfigProjector(
                scope = scope,
                model = config,
                dependencies = dependencies.config(),
            )
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        config
            .collectAsState()
            .value
            .StateContent(
                modifier = Modifier.fillMaxSize(),
                transitionSpec = TransitionSpec.crossfade(),
                label = "Config",
                contentKey = GraphConfigProjector::key,
            ) { pages ->
                pages.Content(
                    contentPadding = contentPadding,
                )
            }
    }
}