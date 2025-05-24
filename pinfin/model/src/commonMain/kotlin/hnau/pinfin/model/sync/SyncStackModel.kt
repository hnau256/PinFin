@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync

import hnau.common.model.goback.GoBackHandler
import hnau.common.model.goback.GoBackHandlerProvider
import hnau.common.model.goback.fallback
import hnau.common.model.stack.NonEmptyStack
import hnau.common.model.stack.StackModelElements
import hnau.common.model.stack.push
import hnau.common.model.stack.stackGoBackHandler
import hnau.common.model.stack.tailGoBackHandler
import hnau.common.model.stack.tryDropLast
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.sync.client.SyncClientStackModel
import hnau.pinfin.model.sync.server.SyncServerModel
import hnau.pinfin.model.sync.start.StartSyncModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class SyncStackModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        fun start(
            syncModeOpener: SyncModeOpener,
        ): StartSyncModel.Dependencies

        fun client(): SyncClientStackModel.Dependencies

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
                        skeleton = SyncClientStackModel.Skeleton(
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

    val stack: StateFlow<NonEmptyStack<SyncStackElementModel>> = StackModelElements(
        scope = scope,
        getKey = SyncStackElementModel.Skeleton::key,
        skeletonsStack = skeleton.stack,
    ) { modelScope, skeleton ->
        createModel(
            modelScope = modelScope,
            skeleton = skeleton,
        )
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
            SyncClientStackModel(
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