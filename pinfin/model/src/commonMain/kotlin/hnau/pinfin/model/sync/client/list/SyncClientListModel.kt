package hnau.pinfin.model.sync.client.list

import arrow.core.Ior
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapReusable
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.map
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.sync.client.utils.TcpSyncClient
import hnau.pinfin.model.sync.utils.SyncHandle
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.repository.BudgetsRepository
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable

class SyncClientListModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val tcpSyncClient: TcpSyncClient

        val budgetsRepository: BudgetsRepository

        fun item(): SyncClientListItemModel.Dependencies
    }

    @Serializable
    /*data*/ class Skeleton

    private val serverBudgetsRequestIndex: MutableStateFlow<Int> =
        0.toMutableStateFlowAsInitial()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val serverBudgets: StateFlow<Loadable<Result<List<SyncHandle.GetBudgets.Response.Budget>>>> =
        serverBudgetsRequestIndex
            .scopedInState(scope)
            .flatMapState(scope) { (attemptScope) ->
                LoadableStateFlow(attemptScope) {
                    dependencies
                        .tcpSyncClient
                        .handle(SyncHandle.GetBudgets)
                        .map(SyncHandle.GetBudgets.Response::budgets)
                }
            }

    fun reload() {
        serverBudgetsRequestIndex.update(Int::inc)
    }

    val items: StateFlow<Loadable<Result<NonEmptyList<Pair<BudgetId, SyncClientListItemModel>>?>>> = combineState(
        scope = scope,
        a = serverBudgets,
        b = dependencies.budgetsRepository.list,
    ) { serverBudgetsOrErrorOrLoading, local ->
        serverBudgetsOrErrorOrLoading.map { serverBudgetsOrError ->
            serverBudgetsOrError.map { serverBudgets ->
                val serverHashes: MutableMap<BudgetId, ServerBudgetPeekHash> = serverBudgets
                    .associate { (id, peekHash) -> id to ServerBudgetPeekHash(peekHash) }
                    .toMutableMap()
                buildList {
                    addAll(
                        local.map { (id, localBudget) ->
                            val serverPeekHash = serverHashes.remove(id)
                            val content = when (serverPeekHash) {
                                null -> Ior.Left(localBudget)
                                else -> Ior.Both(
                                    leftValue = localBudget,
                                    rightValue = serverPeekHash,
                                )
                            }
                            id to content
                        }
                    )
                    addAll(
                        serverHashes
                            .map { (id, serverPeekHash) ->
                                id to Ior.Right(serverPeekHash)
                            }
                    )
                }
                    .sortedBy { it.first }
                    .toNonEmptyListOrNull()
            }
        }
    }.mapReusable(scope) { itemsOrErrorOrLoading ->
        itemsOrErrorOrLoading.map { itemsOrError ->
            itemsOrError.map { items ->
                items?.map { (id, localOrServer: Ior<Deferred<BudgetRepository>, ServerBudgetPeekHash>) ->
                    getOrPutItem(
                        key = id,
                    ) { itemScope ->
                        val itemModel = SyncClientListItemModel(
                            id = id,
                            scope = itemScope,
                            dependencies = dependencies.item(),
                            localOrServer = localOrServer,
                        )
                        id to itemModel
                    }
                }
            }
        }
    }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}