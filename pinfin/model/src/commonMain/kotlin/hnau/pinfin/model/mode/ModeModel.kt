@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.mode

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

class ModeModel(
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
        val state: MutableStateFlow<ModeStateModel.Skeleton> =
            ModeStateModel.Skeleton.Manage(
                skeleton = ManageModel.Skeleton()
            ).toMutableStateFlowAsInitial(),
    )

    val state: StateFlow<ModeStateModel> = skeleton
        .state
        .mapWithScope(scope) { stateScope, stateSkeleton ->
            when (stateSkeleton) {
                is ModeStateModel.Skeleton.Manage -> ModeStateModel.Manage(
                    model = ManageModel(
                        scope = stateScope,
                        dependencies = dependencies.budgets(),
                        skeleton = stateSkeleton.skeleton,
                    )
                )

                is ModeStateModel.Skeleton.Sync -> ModeStateModel.Sync(
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