package hnau.pinfin.projector

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.projector.uikit.state.LoadableContent
import hnau.common.projector.uikit.state.TransitionSpec
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.map
import hnau.pinfin.model.loadbudgets.LoadBudgetsModel
import hnau.pinfin.projector.manage.ManageProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class LoadBudgetsProjector(
    scope: CoroutineScope,
    model: LoadBudgetsModel,
    dependencies: Dependencies,
) {

    @Shuffle
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
    fun Content() {
        budgetsSackProjector
            .collectAsState()
            .value
            .LoadableContent(
                modifier = Modifier.fillMaxSize(),
                transitionSpec = TransitionSpec.horizontal(),
            ) { budgetsStackProjector ->
                budgetsStackProjector.Content()
            }
    }
}