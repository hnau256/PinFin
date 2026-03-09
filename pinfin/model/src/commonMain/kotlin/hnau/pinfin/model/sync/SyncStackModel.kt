@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync

import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.stack.NonEmptyStack
import org.hnau.commons.app.model.stack.SkeletonWithModel
import org.hnau.commons.app.model.stack.goBackHandler
import org.hnau.commons.app.model.stack.modelsOnly
import org.hnau.commons.app.model.stack.push
import org.hnau.commons.app.model.stack.tryDropLast
import org.hnau.commons.app.model.stack.withModels
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.sync.client.SyncClientStackModel
import hnau.pinfin.model.sync.server.SyncServerModel
import hnau.pinfin.model.sync.start.StartSyncModel
import org.hnau.commons.gen.pipe.annotations.Pipe
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
        val stack: MutableStateFlow<NonEmptyStack<SyncStackElementSkeleton>> =
            NonEmptyStack(ElementSkeleton.start(StartSyncModel.Skeleton()))
                .toMutableStateFlowAsInitial(),
    )

    @SealUp(
        variants = [
            Variant(
                type = StartSyncModel::class,
                identifier = "start",
            ),
            Variant(
                type = SyncClientStackModel::class,
                identifier = "client",
            ),
            Variant(
                type = SyncServerModel::class,
                identifier = "server",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "SyncStackElementModel",
    )
    interface Element {

        val goBackHandler: GoBackHandler

        companion object
    }

    @SealUp(
        variants = [
            Variant(
                type = StartSyncModel.Skeleton::class,
                identifier = "start",
            ),
            Variant(
                type = SyncClientStackModel.Skeleton::class,
                identifier = "client",
            ),
            Variant(
                type = SyncServerModel.Skeleton::class,
                identifier = "server",
            ),
        ],
        wrappedValuePropertyName = "skeleton",
        sealedInterfaceName = "SyncStackElementSkeleton",
        serializable = true,
    )
    interface ElementSkeleton {

        companion object
    }

    private val syncModeOpener = run {
        val switchToState: (SyncStackElementSkeleton) -> Unit = { elementSkeleton ->
            skeleton.stack.push(elementSkeleton)
        }
        SyncModeOpener(
            openSyncClient = { address, port ->
                switchToState(
                    ElementSkeleton.client(
                        address = address,
                        port = port,
                    )
                )
            },
            openSyncServer = { port ->
                switchToState(
                    ElementSkeleton.server(
                        port = port,
                    )
                )
            }
        )
    }

    private val stackWithModels: StateFlow<NonEmptyStack<SkeletonWithModel<SyncStackElementSkeleton, SyncStackElementModel>>> =
        skeleton
            .stack
            .withModels(
                scope = scope,
                getKey = SyncStackElementSkeleton::ordinal,
            ) { modelScope, skeleton ->
                createModel(
                    modelScope = modelScope,
                    skeleton = skeleton,
                )
            }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: SyncStackElementSkeleton,
    ): SyncStackElementModel = skeleton.fold(
        ifStart = { startSkeleton ->
            Element.start(
                scope = modelScope,
                dependencies = dependencies.start(
                    syncModeOpener = syncModeOpener,
                ),
                skeleton = startSkeleton,
            )
        },
        ifClient = { clientSkeleton ->
            Element.client(
                scope = modelScope,
                dependencies = dependencies.client(),
                skeleton = clientSkeleton,
            )
        },
        ifServer = { serverSkeleton ->
            Element.server(
                scope = modelScope,
                dependencies = dependencies.server(),
                skeleton = serverSkeleton,
                goBack = { this@SyncStackModel.skeleton.stack.tryDropLast() },
            )
        },
    )

    val stack: StateFlow<NonEmptyStack<SyncStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels.goBackHandler(
        scope = scope,
        extractGoBackHandler = SyncStackElementModel::goBackHandler,
        updateSkeletonStack = skeleton.stack::value::set,
    )
}