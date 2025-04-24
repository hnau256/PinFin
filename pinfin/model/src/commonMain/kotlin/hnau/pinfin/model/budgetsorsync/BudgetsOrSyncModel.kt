@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budgetsorsync

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.budgetsorbudget.BudgetsOrBudgetModel
import hnau.pinfin.model.SyncModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class BudgetsOrSyncModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        fun budgets(): BudgetsOrBudgetModel.Dependencies

        fun sync(): SyncModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val state: MutableStateFlow<BudgetsOrSyncStateModel.Skeleton> =
            BudgetsOrSyncStateModel.Skeleton.Budgets(
                skeleton = BudgetsOrBudgetModel.Skeleton()
            ).toMutableStateFlowAsInitial(),
    )

    val state: StateFlow<BudgetsOrSyncStateModel> = skeleton
        .state
        .mapWithScope(scope) { stateScope, stateSkeleton ->
            when (stateSkeleton) {
                is BudgetsOrSyncStateModel.Skeleton.Budgets -> BudgetsOrSyncStateModel.Budgets(
                    model = BudgetsOrBudgetModel(
                        scope = stateScope,
                        dependencies = dependencies.budgets(),
                        skeleton = stateSkeleton.skeleton,
                    )
                )

                is BudgetsOrSyncStateModel.Skeleton.Sync -> BudgetsOrSyncStateModel.Sync(
                    model = SyncModel(
                        scope = stateScope,
                        dependencies = dependencies.sync(),
                        skeleton = stateSkeleton.skeleton,
                    )
                )
            }
        }

    override val goBackHandler: GoBackHandler = state
        .flatMapState(scope, GoBackHandlerProvider::goBackHandler)
}