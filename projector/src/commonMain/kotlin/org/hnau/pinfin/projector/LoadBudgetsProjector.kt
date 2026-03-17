package org.hnau.pinfin.projector

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.state.TransitionSpec
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.map
import org.hnau.pinfin.model.loadbudgets.LoadBudgetsModel
import org.hnau.pinfin.projector.manage.ManageProjector

class LoadBudgetsProjector(
    scope: CoroutineScope,
    model: LoadBudgetsModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun manage(): ManageProjector.Dependencies
    }

    private val budgetsSackProjector: StateFlow<Loadable<ManageProjector>> = model
        .budgetsOrSync
        .mapWithScope(scope) { scope, budgetsOrSyncModelOrLoading ->
            budgetsOrSyncModelOrLoading.map { budgetsOrSyncModel ->
                ManageProjector(
                    scope = scope,
                    model = budgetsOrSyncModel,
                    dependencies = dependencies.manage(),
                )
            }
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        budgetsSackProjector
            .collectAsState()
            .value
            .LoadableContent(
                modifier = Modifier.fillMaxSize(),
                transitionSpec = TransitionSpec.horizontal(),
            ) { budgetsStackProjector ->
                budgetsStackProjector.Content(
                    contentPadding = contentPadding,
                )
            }
    }
}