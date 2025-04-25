package hnau.pinfin.model.loadbudgets

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.app.preferences.Preferences
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.pinfin.upchain.BudgetsStorage
import hnau.pinfin.model.mode.ModeModel
import hnau.shuffler.annotations.Shuffle
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

    @Shuffle
    interface Dependencies {

        val preferencesFactory: Preferences.Factory

        val budgetsStorageFactory: BudgetsStorage.Factory

        fun budgetsOrSync(
            preferences: Preferences,
            budgetsStorage: BudgetsStorage,
        ): ModeModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var budgetsOrSync: ModeModel.Skeleton? = null,
    )

    private data class Ready(
        val budgetsStorage: BudgetsStorage,
        val preferences: Preferences,
    )

    val budgetsOrSync: StateFlow<Loadable<ModeModel>> = LoadableStateFlow(
        scope = scope,
    ) {
        coroutineScope {
            val deferredBudgetsStorage = async {
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
                budgetsStorage = deferredBudgetsStorage.await(),
                preferences = deferredPreferences.await(),
            )
        }
    }
        .mapWithScope(
            scope = scope,
        ) { stateScope, readyOrLoading ->
            readyOrLoading.map { ready ->
                ModeModel(
                    scope = stateScope,
                    dependencies = dependencies.budgetsOrSync(
                        budgetsStorage = ready.budgetsStorage,
                        preferences = ready.preferences,
                    ),
                    skeleton = skeleton::budgetsOrSync
                        .toAccessor()
                        .getOrInit { ModeModel.Skeleton() },
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