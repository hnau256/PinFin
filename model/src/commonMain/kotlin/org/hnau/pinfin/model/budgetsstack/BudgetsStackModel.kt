@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budgetsstack

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.stack.NonEmptyStack
import org.hnau.commons.app.model.stack.SkeletonWithModel
import org.hnau.commons.app.model.stack.goBackHandler
import org.hnau.commons.app.model.stack.modelsOnly
import org.hnau.commons.app.model.stack.push
import org.hnau.commons.app.model.stack.withModels
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.budgetslist.BudgetsListModel
import org.hnau.pinfin.model.sync.SyncStackModel

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