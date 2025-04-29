package hnau.pinfin.model.sync.client.list

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapListReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toLoadableStateFlow
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.sync.client.BudgetSyncOpener
import hnau.pinfin.model.sync.client.utils.TcpSyncClient
import hnau.pinfin.model.sync.utils.SyncHandle
import hnau.pinfin.model.utils.budget.repository.BudgetsRepository
import hnau.pinfin.model.utils.budget.upchain.Upchain
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable

class SyncClientListModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val budgetOpener: BudgetSyncOpener

        val tcpSyncClient: TcpSyncClient

        val budgetsRepository: BudgetsRepository
    }

    @Serializable
    /*data*/ class Skeleton

    private val serverBudgetsRequestIndex: MutableStateFlow<Int> =
        0.toMutableStateFlowAsInitial()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val serverBudgets: StateFlow<Loadable<Result<List<SyncHandle.GetBudgets.Response.Budget>>>> =
        serverBudgetsRequestIndex
            .mapLatest {
                dependencies
                    .tcpSyncClient
                    .handle(SyncHandle.GetBudgets)
                    .map(SyncHandle.GetBudgets.Response::budgets)
                    .let { Loadable.Ready(it) }
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = Loadable.Loading,
            )

    private data class LocalBudget(
        val peekHash: StateFlow<UpchainHash?>,
    )

    private val localBudgets: StateFlow<List<Pair<BudgetId, StateFlow<Loadable<LocalBudget>>>>> =
        dependencies
            .budgetsRepository
            .list
            .mapListReusable(
                scope = scope,
                extractKey = { it.first },
                transform = { budgetScope, (id, deferredInfo) ->
                    val deferredLocalBudget = budgetScope.async {
                        val info = deferredInfo.await()
                        LocalBudget(
                            peekHash = info
                                .upchainStorage
                                .upchain
                                .mapState(
                                    scope = budgetScope,
                                    transform = Upchain::peekHash,
                                ),
                        )
                    }.toLoadableStateFlow(scope)
                    id to deferredLocalBudget
                }
            )

    val itemsOrEmptyOrErrorOrLoading: StateFlow<Loadable<Result<NonEmptyList<Item>?>>> =
        combineState(
            scope = scope,
            a = serverBudgets,
            b = localBudgets,
        ) { serverBudgetsOrErrorOrLoading, localBudgets ->
            serverBudgetsOrErrorOrLoading to localBudgets
        }.mapWithScope(scope) { stateScope, (serverBudgetsOrErrorOrLoading, localBudgets) ->
            serverBudgetsOrErrorOrLoading.map { serverBudgetsOrError ->
                serverBudgetsOrError.map { serverBudgets ->
                    mergeBudgets(
                        scope = stateScope,
                        server = serverBudgets,
                        local = localBudgets,
                    )
                }
            }
        }

    data class Item(
        val id: BudgetId,
        val state: StateFlow<State>,
    ) {

        sealed interface State {

            data object Loading : State

            data object Actual : State

            data class Syncable(
                val sync: () -> Unit,
                val type: Type,
            ) : State {

                enum class Type { Upload, Download, Sync }
            }
        }
    }

    private fun mergeBudgets(
        scope: CoroutineScope,
        server: List<SyncHandle.GetBudgets.Response.Budget>,
        local: List<Pair<BudgetId, StateFlow<Loadable<LocalBudget>>>>,
    ): NonEmptyList<Item>? {
        val serverHashes: MutableMap<BudgetId, UpchainHash?> = server
            .associate { (id, peekHash) -> id to peekHash }
            .toMutableMap()
        return buildList {
            addAll(
                local.map { (id, localBudget) ->
                    val serverPeekHash = serverHashes.remove(id)
                    Triple(id, localBudget, serverPeekHash)
                }
            )
            addAll(
                serverHashes
                    .map { (id, serverPeekHash) ->
                        Triple(id, null, serverPeekHash)
                    }
            )
        }
            .map { (id, localBudgetOrLoadingOrNull, serverPeekHashOrNull) ->
                val sync: () -> Unit = { dependencies.budgetOpener.openBudgetToSync(id) }
                Item(
                    id = id,
                    state = when (localBudgetOrLoadingOrNull) {
                        null -> Item.State.Syncable(
                            type = Item.State.Syncable.Type.Download,
                            sync = sync,
                        ).toMutableStateFlowAsInitial()

                        else -> when (serverPeekHashOrNull) {
                            null -> Item.State.Syncable(
                                type = Item.State.Syncable.Type.Upload,
                                sync = sync,
                            ).toMutableStateFlowAsInitial()

                            else -> localBudgetOrLoadingOrNull
                                .scopedInState(scope)
                                .flatMapState(scope) { (localBudgetOrLoadingScope, localBudgetOrLoading) ->
                                    localBudgetOrLoading.fold(
                                        ifLoading = { Item.State.Loading.toMutableStateFlowAsInitial() },
                                        ifReady = { localBudget ->
                                            localBudget.peekHash.mapState(
                                                scope = localBudgetOrLoadingScope,
                                            ) { peekHash ->
                                                when (peekHash) {
                                                    serverPeekHashOrNull -> Item.State.Actual
                                                    else -> Item.State.Syncable(
                                                        type = Item.State.Syncable.Type.Sync,
                                                        sync = sync,
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                        }
                    }
                )
            }
            .toNonEmptyListOrNull()
    }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}