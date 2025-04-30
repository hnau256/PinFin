package hnau.pinfin.model.loadbudgets

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.app.preferences.Preferences
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.fold
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.map
import hnau.common.kotlin.toAccessor
import hnau.pinfin.model.mode.ModeModel
import hnau.pinfin.model.utils.budget.repository.BudgetsRepository
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
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
            budgetsRepository: BudgetsRepository,
        ): ModeModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var budgetsOrSync: ModeModel.Skeleton? = null,
    )

    private data class Ready(
        val budgetsRepository: BudgetsRepository,
        val preferences: Preferences,
    )

    val budgetsOrSync: StateFlow<Loadable<ModeModel>> = LoadableStateFlow(
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
                budgetsRepository = BudgetsRepository(
                    scope = scope,
                    budgetsStorage = budgetsStorage.await()
                ),
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
                        budgetsRepository = ready.budgetsRepository,
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