package hnau.pinfin.sync.client

import arrow.core.identity
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapListReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toLoadableStateFlow
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.sync.common.ApiResponse
import hnau.pinfin.sync.common.SyncApi
import hnau.pinfin.sync.common.SyncHandle
import hnau.pinfin.sync.common.UpchainHash
import hnau.pinfin.data.BudgetId
import hnau.pinfin.upchain.BudgetUpchain
import hnau.pinfin.upchain.BudgetsStorage
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class SyncClient(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Shuffle
    interface Dependencies {

        val syncApi: SyncApi

        val budgetsStorage: BudgetsStorage

        fun localBudget(
            upchain: BudgetUpchain,
        ): LocalBudget.Dependencies
    }

    @Serializable
    data class Skeleton(
        val selectedBudget: MutableStateFlow<BudgetId?> =
            null.toMutableStateFlowAsInitial(),
        val error: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),
    )

    val state: StateFlow<SyncClientState> = skeleton
        .error
        .scopedInState(scope)
        .flatMapState(scope) { (errorOrNotScope, error) ->
            when (error) {
                true -> SyncClientState.Error.toMutableStateFlowAsInitial()
                false -> getNotErrorState(
                    scope = errorOrNotScope,
                )
            }
        }

    private fun getNotErrorState(
        scope: CoroutineScope,
    ): StateFlow<SyncClientState> = combineState(
        scope = scope,
        a = dependencies.budgetsStorage.list,
        b = skeleton.selectedBudget,
    ) { budgets, selectedOrNull ->
        selectedOrNull
            ?.let { selected ->
                budgets
                    .firstOrNull { it.first == selected }
                    ?.second
            }
    }
        .scopedInState(scope)
        .flatMapState(scope) { (listOrBudgetScope, selectedBudgetUpchain) ->
            when (selectedBudgetUpchain) {
                null -> getInitializingOrListState(
                    scope = listOrBudgetScope,
                )

                else -> getSynchronizingState(
                    scope = listOrBudgetScope,
                    selectedBudgetUpchain = selectedBudgetUpchain,
                )
            }
        }

    private val localBudgets: StateFlow<Map<BudgetId, Deferred<LocalBudget>>> = dependencies
        .budgetsStorage
        .list
        .mapListReusable(
            scope = scope,
            extractKey = { it.first },
            transform = { budgetScope, (id, upchain) ->
                val budget = budgetScope.async {
                    LocalBudget.create(
                        scope = budgetScope,
                        dependencies = dependencies.localBudget(
                            upchain = upchain,
                        ),
                    )
                }
                id to budget
            }
        )
        .mapState(scope) { budgets ->
            budgets.associate(::identity)
        }

    private fun getInitializingOrListState(
        scope: CoroutineScope,
    ): StateFlow<SyncClientState> = combineState(
        scope = scope,
        a = localBudgets,
        b = LoadableStateFlow(scope) {
            dependencies
                .syncApi
                .handle(SyncHandle.GetBudgets)
        }
    ) { localBudgets, serverBudgetsOrLoading ->
        localBudgets to serverBudgetsOrLoading
    }
        .mapWithScope(scope) { stateScope, (localBudgets, serverBudgetsOrLoading) ->
            when (serverBudgetsOrLoading) {
                Loadable.Loading -> SyncClientState.Initializing
                is Loadable.Ready -> when (val serverBudgets = serverBudgetsOrLoading.value) {
                    is ApiResponse.Error -> SyncClientState.Error
                    is ApiResponse.Success -> joinLocalAndServerBudgets(
                        scope = stateScope,
                        localBudgets = localBudgets,
                        serverBudgets = serverBudgets.data,
                    )
                }
            }
        }

    private fun joinLocalAndServerBudgets(
        scope: CoroutineScope,
        localBudgets: Map<BudgetId, Deferred<LocalBudget>>,
        serverBudgets: SyncHandle.GetBudgets.Response,
    ): SyncClientState.Budgets {
        val serverHashes: MutableMap<BudgetId, UpchainHash> = serverBudgets
            .budgets
            .associate { (id, peekHash) -> id to peekHash }
            .toMutableMap()
        return SyncClientState.Budgets(
            items = buildList {
                addAll(
                    localBudgets.map { (id, localBudget) ->
                        val localPeekHash = localBudget
                            .toLoadableStateFlow(scope)
                            .flatMapState(scope) {
                                it.orNull()?.peekHash ?: null.toMutableStateFlowAsInitial()
                            }
                        val serverPeekHash = serverHashes.remove(id)
                        Triple(id, localPeekHash, serverPeekHash)
                    }
                )
                addAll(
                    serverHashes
                        .map { (id, serverPeekHash) ->
                            Triple(id, null, serverPeekHash)
                        }
                )
            }
                .map { (id, localPeekHashOrNull, serverPeekHashOrNull) ->
                    val sync: () -> Unit = {  skeleton.selectedBudget.value = id }
                    SyncClientState.Budgets.Item(
                        id = id,
                        state = when (localPeekHashOrNull) {
                            null -> SyncClientState.Budgets.Item.State.CanBeeSynchronized(
                                state = SyncClientState.Budgets.Item.State.CanBeeSynchronized.State.OnlyOnServer,
                                sync = sync,
                            ).toMutableStateFlowAsInitial()

                            else -> when (serverPeekHashOrNull) {
                                null -> SyncClientState.Budgets.Item.State.CanBeeSynchronized(
                                    state = SyncClientState.Budgets.Item.State.CanBeeSynchronized.State.OnlyLocal,
                                    sync = sync,
                                ).toMutableStateFlowAsInitial()

                                else -> localPeekHashOrNull.mapState(scope) { localPeekHash ->
                                    when (localPeekHash) {
                                        null -> SyncClientState.Budgets.Item.State.Loading
                                        serverPeekHashOrNull -> SyncClientState.Budgets.Item.State.Actual
                                        else -> SyncClientState.Budgets.Item.State.CanBeeSynchronized(
                                            state = SyncClientState.Budgets.Item.State.CanBeeSynchronized.State.LocalAndOnServer,
                                            sync = sync,
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
        )
    }

    private fun getSynchronizingState(
        scope: CoroutineScope,
        selectedBudgetUpchain: BudgetUpchain,
    ): StateFlow<SyncClientState.Syncronizing> {

    }

}

