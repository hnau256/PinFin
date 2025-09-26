@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.SkeletonWithModel
import hnau.common.app.model.stack.goBackHandler
import hnau.common.app.model.stack.modelsOnly
import hnau.common.app.model.stack.push
import hnau.common.app.model.stack.tryDropLast
import hnau.common.app.model.stack.withModels
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
) {

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

    private val stackWithModels: StateFlow<NonEmptyStack<SkeletonWithModel<SyncStackElementModel.Skeleton, SyncStackElementModel>>> =
        skeleton
            .stack
            .withModels(
                scope = scope,
                getKey = SyncStackElementModel.Skeleton::key,
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

    val stack: StateFlow<NonEmptyStack<SyncStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels.goBackHandler(
        scope = scope,
        extractGoBackHandler = SyncStackElementModel::goBackHandler,
        updateSkeletonStack = skeleton.stack::value::set,
    )
}