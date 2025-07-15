package hnau.pinfin.projector.sync

import androidx.compose.runtime.Composable
import hnau.common.app.projector.stack.Content
import hnau.common.app.projector.stack.StackProjectorTail
import hnau.pinfin.model.sync.SyncStackElementModel
import hnau.pinfin.model.sync.SyncStackModel
import hnau.pinfin.projector.sync.client.SyncClientStackProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class SyncStackProjector(
    scope: CoroutineScope,
    model: SyncStackModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun start(): StartSyncProjector.Dependencies

        fun client(): SyncClientStackProjector.Dependencies

        fun server(): SyncServerProjector.Dependencies
    }

    private val tail: StateFlow<StackProjectorTail<Int, SyncStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = { model -> model.key },
            createProjector = { scope, model ->
                when (model) {
                    is SyncStackElementModel.Start -> SyncStackElementProjector.Start(
                        StartSyncProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.start(),
                        )
                    )

                    is SyncStackElementModel.Client -> SyncStackElementProjector.Client(
                        SyncClientStackProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.client(),
                        )
                    )

                    is SyncStackElementModel.Server -> SyncStackElementProjector.Server(
                        SyncServerProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.server(),
                        )
                    )
                }
            }
        )

    @Composable
    fun Content() {
        tail.Content { elementProjector ->
            elementProjector.Content()
        }
    }
}