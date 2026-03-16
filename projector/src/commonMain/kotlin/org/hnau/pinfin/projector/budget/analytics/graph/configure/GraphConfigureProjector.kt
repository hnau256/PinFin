package org.hnau.pinfin.projector.budget.analytics.graph.configure

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.GraphConfigureModel

class GraphConfigureProjector(
    scope: CoroutineScope,
    model: GraphConfigureModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {
    }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
     TODO()
    }
}