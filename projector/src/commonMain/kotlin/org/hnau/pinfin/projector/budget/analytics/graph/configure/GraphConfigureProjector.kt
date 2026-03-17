package org.hnau.pinfin.projector.budget.analytics.graph.configure

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.budget.analytics.tab.graph.configure.GraphConfigureModel
import org.hnau.pinfin.projector.budget.analytics.graph.configure.period.ConfigSplitPeriodProjector

class GraphConfigureProjector(
    scope: CoroutineScope,
    private val model: GraphConfigureModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun configSpitPeriod(): ConfigSplitPeriodProjector.Dependencies
    }

    private val splitPeriod = ConfigSplitPeriodProjector(
        scope = scope,
        model = model.period,
        dependencies = dependencies.configSpitPeriod(),
    )

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                contentPadding = contentPadding,
            ) {
                item(
                    key = "SplitPeriod",
                ) {
                    splitPeriod.Content(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(Dimens.largeSeparation),
                contentAlignment = Alignment.BottomEnd,
            ) {
                FloatingActionButton(
                    onClick = { model.save.value?.invoke() },
                ) {
                    Icon(Icons.Filled.Save)
                }
            }
        }
    }
}