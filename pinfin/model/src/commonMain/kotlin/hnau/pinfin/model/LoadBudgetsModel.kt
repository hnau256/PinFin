package hnau.pinfin.model

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.storage.BudgetsStorage
import hnau.pinfin.model.budgetsorsync.ManageOrSyncModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class LoadBudgetsModel(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val budgetsStorageFactory: BudgetsStorage.Factory

        fun budgetsOrSync(
            budgetsStorage: BudgetsStorage,
        ): ManageOrSyncModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var budgetsOrSync: ManageOrSyncModel.Skeleton? = null,
    )

    val budgetsOrSync: StateFlow<Loadable<ManageOrSyncModel>> = LoadableStateFlow(
        scope = scope,
    ) {
        dependencies.budgetsStorageFactory.createBudgetsStorage(
            scope = scope,
        )
    }
        .mapWithScope(
            scope = scope,
        ) { stateScope, budgetsStorageOrLoading ->
            budgetsStorageOrLoading.map { budgetsStorage ->
                ManageOrSyncModel(
                    scope = stateScope,
                    dependencies = dependencies.budgetsOrSync(
                        budgetsStorage = budgetsStorage,
                    ),
                    skeleton = skeleton::budgetsOrSync
                        .toAccessor()
                        .getOrInit { ManageOrSyncModel.Skeleton() },
                )
            }
        }

    override val goBackHandler: StateFlow<(() -> Unit)?> = budgetsOrSync
        .flatMapState(scope) { currentMainModel ->
            currentMainModel.fold(
                ifLoading = { NeverGoBackHandler },
                ifReady = GoBackHandlerProvider::goBackHandler,
            )
        }
}