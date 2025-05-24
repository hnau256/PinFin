package hnau.pinfin.model.loadbudgets

import hnau.common.model.goback.GoBackHandlerProvider
import hnau.common.model.goback.NeverGoBackHandler
import hnau.common.model.preferences.Preferences
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.fold
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.map
import hnau.common.kotlin.toAccessor
import hnau.pinfin.model.manage.ManageModel
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class LoadBudgetsModel(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        val preferencesFactory: Preferences.Factory

        val budgetsStorageFactory: BudgetsStorage.Factory

        fun manage(
            preferences: Preferences,
            budgetsStorage: BudgetsStorage,
        ): ManageModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var manage: ManageModel.Skeleton? = null,
    )

    private data class Ready(
        val budgetsStorage: BudgetsStorage,
        val preferences: Preferences,
    )

    val budgetsOrSync: StateFlow<Loadable<ManageModel>> = LoadableStateFlow(
        scope = scope,
    ) {
        coroutineScope {
            val budgetsStorage = async {
                dependencies.budgetsStorageFactory.createBudgetsStorage(
                    scope = scope,
                )
            }
            val deferredPreferences = async {
                dependencies.preferencesFactory.createPreferences(
                    scope = scope,
                )
            }
            Ready(
                budgetsStorage = budgetsStorage.await(),
                preferences = deferredPreferences.await(),
            )
        }
    }
        .mapWithScope(
            scope = scope,
        ) { stateScope, readyOrLoading ->
            readyOrLoading.map { ready ->
                ManageModel(
                    scope = stateScope,
                    dependencies = dependencies.manage(
                        budgetsStorage = ready.budgetsStorage,
                        preferences = ready.preferences,
                    ),
                    skeleton = skeleton::manage
                        .toAccessor()
                        .getOrInit { ManageModel.Skeleton() },
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