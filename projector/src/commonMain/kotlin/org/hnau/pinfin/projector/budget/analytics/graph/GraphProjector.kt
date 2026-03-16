package org.hnau.pinfin.projector.budget.analytics.graph

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.state.TransitionSpec
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.pinfin.model.budget.analytics.tab.graph.GraphModel
import org.hnau.pinfin.model.budget.analytics.tab.graph.fold
import org.hnau.pinfin.projector.budget.analytics.graph.configure.GraphConfigureProjector
import org.hnau.pinfin.projector.budget.analytics.graph.configured.GraphConfiguredProjector

class GraphProjector(
    scope: CoroutineScope,
    model: GraphModel,
    dependencies: Dependencies,
) {

    @SealUp(
        variants = [
            Variant(
                type = GraphConfiguredProjector::class,
                identifier = "configured",
            ),
            Variant(
                type = GraphConfigureProjector::class,
                identifier = "configure",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "GraphStateProjector",
    )
    interface State {

        @Composable
        fun Content(
            contentPadding: PaddingValues,
        )

        companion object
    }

    @Pipe
    interface Dependencies {

        fun configured(): GraphConfiguredProjector.Dependencies

        fun configure(): GraphConfigureProjector.Dependencies
    }

    private val state: StateFlow<GraphStateProjector> = model
        .state
        .mapWithScope(scope) { scope, state ->
            state.fold(
                ifConfigured = { model ->
                    State.configured(
                        scope = scope,
                        model = model,
                        dependencies = dependencies.configured(),
                    )
                },
                ifConfigure = { model ->
                    State.configure(
                        scope = scope,
                        model = model,
                        dependencies = dependencies.configure(),
                    )
                }
            )
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        state
            .collectAsState()
            .value
            .StateContent(
                modifier = Modifier.fillMaxSize(),
                label = "configuredOrConfigure",
                contentKey = {it.ordinal},
                transitionSpec = TransitionSpec.both(),
            ) {state ->
                state.Content(contentPadding)
            }
    }
}