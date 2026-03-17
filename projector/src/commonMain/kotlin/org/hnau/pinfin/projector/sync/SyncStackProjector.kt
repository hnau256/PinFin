package org.hnau.pinfin.projector.sync

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.stack.Content
import org.hnau.commons.app.projector.stack.StackProjectorTail
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.pinfin.model.sync.SyncStackModel
import org.hnau.pinfin.model.sync.fold
import org.hnau.pinfin.projector.sync.client.SyncClientStackProjector

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

    @SealUp(
        variants = [
            Variant(
                type = StartSyncProjector::class,
                identifier = "start",
            ),
            Variant(
                type = SyncClientStackProjector::class,
                identifier = "client",
            ),
            Variant(
                type = SyncServerProjector::class,
                identifier = "server",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "SyncPageProjector",
    )
    interface PageProjector {

        @Composable
        fun Content(
            contentPadding: PaddingValues,
        )

        companion object
    }

    private val tail: StateFlow<StackProjectorTail<Int, SyncPageProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = { model -> model.ordinal },
            createProjector = { scope, model ->
                model.fold(
                    ifStart = { startModel ->
                        PageProjector.start(
                            model = startModel,
                            dependencies = dependencies.start(),
                        )
                    },
                    ifClient = { clientModel ->
                        PageProjector.client(
                            scope = scope,
                            model = clientModel,
                            dependencies = dependencies.client(),
                        )
                    },
                    ifServer = { serverModel ->
                        PageProjector.server(
                            model = serverModel,
                            dependencies = dependencies.server(),
                        )
                    },
                )
            }
        )

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        tail.Content { elementProjector ->
            elementProjector.Content(
                contentPadding = contentPadding,
            )
        }
    }
}