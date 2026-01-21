@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budgetsstack

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.SkeletonWithModel
import hnau.common.app.model.stack.goBackHandler
import hnau.common.app.model.stack.modelsOnly
import hnau.common.app.model.stack.push
import hnau.common.app.model.stack.withModels
import hnau.common.gen.sealup.annotations.SealUp
import hnau.common.gen.sealup.annotations.Variant
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.budgetslist.BudgetsListModel
import hnau.pinfin.model.sync.SyncStackModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class BudgetsStackModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) {

    @Serializable
    data class Skeleton(
        val stack: MutableStateFlow<NonEmptyStack<BudgetsStackElementSkeleton>> =
            MutableStateFlow(NonEmptyStack(ElementSkeleton.list(Unit))),
    )

    @Pipe
    interface Dependencies {

        fun list(
            syncOpener: SyncOpener,
        ): BudgetsListModel.Dependencies

        fun sync(): SyncStackModel.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = BudgetsListModel::class,
                identifier = "list",
            ),
            Variant(
                type = SyncStackModel::class,
                identifier = "sync",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "BudgetsStackElementModel",
    )
    interface Element {

        val goBackHandler: GoBackHandler

        companion object
    }

    @SealUp(
        variants = [
            Variant(
                type = Unit::class,
                identifier = "list",
            ),
            Variant(
                type = SyncStackModel.Skeleton::class,
                identifier = "sync",
            ),
        ],
        wrappedValuePropertyName = "skeleton",
        sealedInterfaceName = "BudgetsStackElementSkeleton",
        serializable = true,
    )
    interface ElementSkeleton {

        companion object
    }

    private val stackWithModels: StateFlow<NonEmptyStack<SkeletonWithModel<BudgetsStackElementSkeleton, BudgetsStackElementModel>>> =
        skeleton
            .stack
            .withModels(
                scope = scope,
                getKey = BudgetsStackElementSkeleton::ordinal,
            ) { modelScope, skeleton ->
                createModel(
                    modelScope = modelScope,
                    skeleton = skeleton,
                )
            }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: BudgetsStackElementSkeleton,
    ): BudgetsStackElementModel = skeleton.fold(
        ifList = {
            Element.list(
                scope = modelScope,
                dependencies = dependencies.list(
                    syncOpener = {
                        this@BudgetsStackModel
                            .skeleton
                            .stack
                            .push(ElementSkeleton.sync())
                    }
                ),
            )
        },
        ifSync = { syncSkeleton ->
            Element.sync(
                scope = modelScope,
                skeleton = syncSkeleton,
                dependencies = dependencies.sync(),
            )
        },
    )

    val stack: StateFlow<NonEmptyStack<BudgetsStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels
        .goBackHandler(
            scope = scope,
            extractGoBackHandler = BudgetsStackElementModel::goBackHandler,
            updateSkeletonStack = skeleton.stack::value::set,
        )
}