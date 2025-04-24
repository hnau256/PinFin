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
import hnau.pinfin.model.manage.ManageModel
import hnau.pinfin.model.SyncModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class ManageOrSyncModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        fun budgets(): ManageModel.Dependencies

        fun sync(): SyncModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val state: MutableStateFlow<ManageOrSyncStateModel.Skeleton> =
            ManageOrSyncStateModel.Skeleton.Manage(
                skeleton = ManageModel.Skeleton()
            ).toMutableStateFlowAsInitial(),
    )

    val state: StateFlow<ManageOrSyncStateModel> = skeleton
        .state
        .mapWithScope(scope) { stateScope, stateSkeleton ->
            when (stateSkeleton) {
                is ManageOrSyncStateModel.Skeleton.Manage -> ManageOrSyncStateModel.Manage(
                    model = ManageModel(
                        scope = stateScope,
                        dependencies = dependencies.budgets(),
                        skeleton = stateSkeleton.skeleton,
                    )
                )

                is ManageOrSyncStateModel.Skeleton.Sync -> ManageOrSyncStateModel.Sync(
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