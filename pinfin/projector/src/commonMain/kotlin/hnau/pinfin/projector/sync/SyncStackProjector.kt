package hnau.pinfin.projector.sync

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import hnau.common.app.projector.stack.Content
import hnau.common.app.projector.stack.StackProjectorTail
import hnau.common.gen.sealup.annotations.SealUp
import hnau.common.gen.sealup.annotations.Variant
import hnau.pinfin.model.sync.SyncStackElementModel
import hnau.pinfin.model.sync.SyncStackModel
import hnau.pinfin.model.sync.fold
import hnau.pinfin.projector.budget.analytics.AnalyticsProjector
import hnau.pinfin.projector.budget.config.BudgetConfigProjector
import hnau.pinfin.projector.budget.transactions.TransactionsProjector
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
    interface Page {

        @Composable
        fun Content()

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
                        Page.start(
                            scope = scope,
                            model = startModel,
                            dependencies = dependencies.start(),
                        )
                    },
                    ifClient = { clientModel ->
                        Page.client(
                            scope = scope,
                            model = clientModel,
                            dependencies = dependencies.client(),
                        )
                    },
                    ifServer ={ serverModel ->
                        Page.server(
                            scope = scope,
                            model = serverModel,
                            dependencies = dependencies.server(),
                        )
                    },
                )
            }
        )

    @Composable
    fun Content() {
        tail.Content { elementProjector ->
            elementProjector.Content()
        }
    }
}