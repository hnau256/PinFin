@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budgetsstack

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.fallback
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.StackModelElements
import hnau.common.app.model.stack.push
import hnau.common.app.model.stack.stackGoBackHandler
import hnau.common.app.model.stack.tailGoBackHandler
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.budgetslist.BudgetsListModel
import hnau.pinfin.model.sync.SyncStackModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class BudgetsStackModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val stack: MutableStateFlow<NonEmptyStack<BudgetsStackElementModel.Skeleton>> =
            MutableStateFlow(NonEmptyStack(BudgetsStackElementModel.Skeleton.List())),
    )

    @Shuffle
    interface Dependencies {

        fun list(
            syncOpener: SyncOpener,
        ): BudgetsListModel.Dependencies

        fun sync(): SyncStackModel.Dependencies
    }

    val stack: StateFlow<NonEmptyStack<BudgetsStackElementModel>> = run {
        val stack = skeleton.stack
        StackModelElements(
            scope = scope,
            getKey = BudgetsStackElementModel.Skeleton::key,
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
        skeleton: BudgetsStackElementModel.Skeleton,
    ): BudgetsStackElementModel = when (skeleton) {
        is BudgetsStackElementModel.Skeleton.List -> BudgetsStackElementModel.List(
            BudgetsListModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
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

    override val goBackHandler: GoBackHandler = stack
        .tailGoBackHandler(scope)
        .fallback(
            scope = scope,
            fallback = skeleton.stack.stackGoBackHandler(scope),
        )
}