package hnau.pinfin.sync.client

import hnau.pinfin.upchain.BudgetId
import kotlinx.coroutines.flow.StateFlow

sealed interface SyncClientState {

    data object Initializing: SyncClientState

    data object Error: SyncClientState

    data class Budgets(
        val items: List<Item>,
    ): SyncClientState {

        data class Item(
            val id: BudgetId,
            val state: StateFlow<State>,
        ) {

            sealed interface State {

                data object Loading: State

                data object Actual: State

                data class CanBeeSynchronized(
                    val state: State,
                    val sync: () -> Unit,
                ): State {

                    enum class State {
                        OnlyLocal,
                        OnlyOnServer,
                        LocalAndOnServer,
                    }
                }
            }
        }
    }

    data class Syncronizing(
        val id: BudgetId,
    ) : SyncClientState {

        sealed interface State {

            data object InProgress : State

            data class Ready(
                val goBack: () -> Unit,
            ) : State
        }
    }
}