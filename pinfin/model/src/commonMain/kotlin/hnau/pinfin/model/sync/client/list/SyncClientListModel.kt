package hnau.pinfin.model.sync.client.list

import arrow.core.Ior
import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.mapReusable
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.map
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.sync.client.utils.TcpSyncClient
import hnau.pinfin.model.sync.utils.SyncHandle
import hnau.pinfin.model.utils.budget.repository.BudgetInfo
import hnau.pinfin.model.utils.budget.repository.BudgetsRepository
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
            .mapLatest {
                dependencies
                    .tcpSyncClient
                    .handle(SyncHandle.GetBudgets)
                    .map(SyncHandle.GetBudgets.Response::budgets)
                    .let(::Ready)
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = Loading,
            )

    val items: StateFlow<Loadable<Result<List<SyncClientListItemModel>>>> = combineState(
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
                }.sortedBy { it.first }
            }
        }
    }.mapReusable(scope) { itemsOrErrorOrLoading ->
        itemsOrErrorOrLoading.map { itemsOrError ->
            itemsOrError.map { items ->
                items.map { (id, localOrServer: Ior<Deferred<BudgetInfo>, ServerBudgetPeekHash>) ->
                    getOrPutItem(
                        key = id,
                    ) { itemScope ->
                        SyncClientListItemModel(
                            id = id,
                            scope = itemScope,
                            dependencies = dependencies.item(),
                            localOrServer = localOrServer,
                        )
                    }
                }
            }
        }
    }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}