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
        val stack: MutableStateFlow<NonEmptyStack<BudgetsStackElementModel.Skeleton>> =
            MutableStateFlow(NonEmptyStack(BudgetsStackElementModel.Skeleton.List)),
    )

    @Pipe
    interface Dependencies {

        fun list(
            syncOpener: SyncOpener,
        ): BudgetsListModel.Dependencies

        fun sync(): SyncStackModel.Dependencies
    }

    private val stackWithModels: StateFlow<NonEmptyStack<SkeletonWithModel<BudgetsStackElementModel.Skeleton, BudgetsStackElementModel>>> =
        skeleton
            .stack
            .withModels(
                scope = scope,
                getKey = BudgetsStackElementModel.Skeleton::key,
            ) { modelScope, skeleton ->
                createModel(
                    modelScope = modelScope,
                    skeleton = skeleton,
                )
            }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: BudgetsStackElementModel.Skeleton,
    ): BudgetsStackElementModel = when (skeleton) {
        is BudgetsStackElementModel.Skeleton.List -> BudgetsStackElementModel.List(
            BudgetsListModel(
                scope = modelScope,
                dependencies = dependencies.list(
                    syncOpener = {
                        this@BudgetsStackModel
                            .skeleton
                            .stack
                            .push(BudgetsStackElementModel.Skeleton.Sync())
                    }
                ),
            )
        )

        is BudgetsStackElementModel.Skeleton.Sync -> BudgetsStackElementModel.Sync(
            SyncStackModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.sync(),
            )
        )
    }

    val stack: StateFlow<NonEmptyStack<BudgetsStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels
        .goBackHandler(
            scope = scope,
            extractGoBackHandler = BudgetsStackElementModel::goBackHandler,
            updateSkeletonStack = skeleton.stack::value::set,
        )
}