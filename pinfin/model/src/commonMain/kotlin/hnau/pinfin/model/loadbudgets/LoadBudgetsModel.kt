package hnau.pinfin.model.loadbudgets

import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.preferences.Preferences
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.LoadableStateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.fold
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.map
import org.hnau.commons.kotlin.toAccessor
import hnau.pinfin.model.manage.ManageModel
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import org.hnau.commons.gen.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class LoadBudgetsModel(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) {

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
        ) { scope, readyOrLoading ->
            readyOrLoading.map { ready ->
                ManageModel(
                    scope = scope,
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

    val goBackHandler: StateFlow<(() -> Unit)?> = budgetsOrSync
        .flatMapState(scope) { currentMainModel ->
            currentMainModel.fold(
                ifLoading = { NeverGoBackHandler },
                ifReady = ManageModel::goBackHandler,
            )
        }
}