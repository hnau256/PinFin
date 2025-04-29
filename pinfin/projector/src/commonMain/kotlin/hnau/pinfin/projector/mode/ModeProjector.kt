package hnau.pinfin.projector.mode

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.state.StateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.mode.ModeModel
import hnau.pinfin.model.mode.ModeStateModel
import hnau.pinfin.projector.sync.SyncStackProjector
import hnau.pinfin.projector.manage.ManageProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class ModeProjector(
    scope: CoroutineScope,
    model: ModeModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun manage(): ManageProjector.Dependencies

        fun sync(): SyncStackProjector.Dependencies
    }

    private val state: StateFlow<ModeStateProjector> = model
        .state
        .mapWithScope(scope) { stateScope, state ->
            when (state) {
                is ModeStateModel.Manage -> ModeStateProjector.Manage(
                    projector = ManageProjector(
                        scope = stateScope,
                        dependencies = dependencies.manage(),
                        model = state.model,
                    )
                )

                is ModeStateModel.Sync -> ModeStateProjector.Sync(
                    projector = SyncStackProjector(
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
                contentKey = ModeStateProjector::key,
            ) { elementProjector ->
                elementProjector.Content()
            }
    }
}