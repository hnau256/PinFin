package org.hnau.pinfin.projector.budget.analytics.graph

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.state.TransitionSpec
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.map
import org.hnau.pinfin.model.budget.analytics.tab.graph.GraphModel
import org.hnau.commons.gen.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class GraphProjector(
    scope: CoroutineScope,
    model: GraphModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun pages(): GraphPagesProjector.Dependencies
    }

    private val pages: StateFlow<Loadable<GraphPagesProjector>> =
        model.pages.mapWithScope(scope) { scope, pagesOrLoading ->
            pagesOrLoading.map { (_, pages) -> //TODO handle delayed
                GraphPagesProjector(
                    scope = scope,
                    model = pages,
                    dependencies = dependencies.pages(),
                )
            }
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        pages
            .collectAsState()
            .value
            .LoadableContent(
                modifier = Modifier.fillMaxSize(),
                transitionSpec = TransitionSpec.crossfade(),
            ) { pages ->
                pages.Content(
                    contentPadding = contentPadding,
                )
            }
    }
}