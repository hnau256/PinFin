package hnau.pinfin.projector.manageorsync

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.state.StateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.budgetsorsync.ManageOrSyncModel
import hnau.pinfin.model.budgetsorsync.ManageOrSyncStateModel
import hnau.pinfin.projector.SyncProjector
import hnau.pinfin.projector.manage.ManageProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class ManageOrSyncProjector(
    scope: CoroutineScope,
    model: ManageOrSyncModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun manage(): ManageProjector.Dependencies

        fun sync(): SyncProjector.Dependencies
    }

    private val state: StateFlow<ManageOrSyncElementProjector> = model
        .state
        .mapWithScope(scope) { stateScope, state ->
            when (state) {
                is ManageOrSyncStateModel.Manage -> ManageOrSyncElementProjector.Manage(
                    projector = ManageProjector(
                        scope = stateScope,
                        dependencies = dependencies.manage(),
                        model = state.model,
                    )
                )

                is ManageOrSyncStateModel.Sync -> ManageOrSyncElementProjector.Sync(
                    projector = SyncProjector(
                        scope = stateScope,
                        dependencies = dependencies.sync(),
                        model = state.model,
                    )
                )
            }
        }

    @Composable
    fun Content() {
        state
            .collectAsState()
            .value
            .StateContent(
                modifier = Modifier.fillMaxSize(),
                label = "BudgetsOrSync",
                transitionSpec = TransitionSpec.crossfade(),
                contentKey = ManageOrSyncElementProjector::key,
            ) { elementProjector ->
                elementProjector.Content()
            }
    }
}