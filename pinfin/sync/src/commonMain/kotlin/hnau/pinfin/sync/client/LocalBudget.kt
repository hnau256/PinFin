package hnau.pinfin.sync.client

import hnau.common.kotlin.coroutines.mapStateLite
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.model.sync.utils.UpchainHash
import hnau.pinfin.model.sync.utils.UpchainState
import hnau.pinfin.upchain.BudgetUpchain
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocalBudget private constructor(
    scope: CoroutineScope,
    dependencies: Dependencies,
    initialState: UpchainState,
) {

    @Shuffle
    interface Dependencies {

        val upchain: BudgetUpchain
    }

    private val state: MutableStateFlow<UpchainState> = initialState.toMutableStateFlowAsInitial()

    val peekHash: StateFlow<UpchainHash> =
        state.mapStateLite(UpchainState::peekHash)

    companion object {

        suspend fun create(
            scope: CoroutineScope,
            dependencies: Dependencies,
        ): LocalBudget {
            val initialState = dependencies.upchain.useUpdates { updates ->
                updates.fold(
                    initial = UpchainState.empty,
                ) { state, update ->
                    state + update
                }
            }
            return LocalBudget(
                scope = scope,
                dependencies = dependencies,
                initialState = initialState,
            )
        }
    }
}