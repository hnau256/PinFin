package hnau.pinfin.model.sync

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.fallback
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.StackModelElements
import hnau.common.app.model.stack.push
import hnau.common.app.model.stack.stackGoBackHandler
import hnau.common.app.model.stack.tailGoBackHandler
import hnau.common.app.model.stack.tryDropLast
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.model.sync.client.SyncClientModel
import hnau.pinfin.model.sync.server.SyncServerModel
import hnau.pinfin.model.sync.start.StartSyncModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class SyncStackModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        fun start(
            syncModeOpener: SyncModeOpener,
        ): StartSyncModel.Dependencies

        fun client(): SyncClientModel.Dependencies

        fun server(): SyncServerModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val stack: MutableStateFlow<NonEmptyStack<SyncStackElementModel.Skeleton>> =
            NonEmptyStack(SyncStackElementModel.Skeleton.Start()).toMutableStateFlowAsInitial(),
    )

    private val syncModeOpener = run {
        val switchToState: (SyncStackElementModel.Skeleton) -> Unit = { elementSkeleton ->
            skeleton.stack.push(elementSkeleton)
        }
        SyncModeOpener(
            openSyncClient = { address, port ->
                switchToState(
                    SyncStackElementModel.Skeleton.Client(
                        skeleton = SyncClientModel.Skeleton(
                            address = address,
                            port = port,
                        )
                    )
                )
            },
            openSyncServer = { port ->
                switchToState(
                    SyncStackElementModel.Skeleton.Server(
                        skeleton = SyncServerModel.Skeleton(
                            port = port,
                        )
                    )
                )
            }
        )
    }

    val stack: StateFlow<NonEmptyStack<SyncStackElementModel>> = run {
        val stack = skeleton.stack
        StackModelElements(
            scope = scope,
            skeletonsStack = stack,
        ) { modelScope, skeleton ->
            createModel(
                modelScope = modelScope,
                skeleton = skeleton,
            )
        }
    }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: SyncStackElementModel.Skeleton,
    ): SyncStackElementModel = when (skeleton) {
        is SyncStackElementModel.Skeleton.Start -> SyncStackElementModel.Start(
            StartSyncModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.start(
                    syncModeOpener = syncModeOpener,
                ),
            )
        )

        is SyncStackElementModel.Skeleton.Server -> SyncStackElementModel.Server(
            SyncServerModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.server(),
                goBack = { this@SyncStackModel.skeleton.stack.tryDropLast() }
            )
        )

        is SyncStackElementModel.Skeleton.Client -> SyncStackElementModel.Client(
            SyncClientModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.client(),
            )
        )
    }

    override val goBackHandler: GoBackHandler = stack
        .tailGoBackHandler(scope)
        .fallback(
            scope = scope,
            fallback = skeleton.stack.stackGoBackHandler(scope),
        )
}