package hnau.pinfin.model.sync.client.list

import arrow.core.Ior
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.flatMapWithScope
import hnau.common.kotlin.coroutines.mapReusable
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.map
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.sync.client.utils.TcpSyncClient
import hnau.pinfin.model.sync.utils.SyncHandle
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.BudgetInfo
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
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

    @Pipe
    interface Dependencies {

        val tcpSyncClient: TcpSyncClient

        val budgetsStorage: BudgetsStorage

        fun item(): SyncClientListItemModel.Dependencies
    }

    @Serializable
    /*data*/ class Skeleton

    private val serverBudgetsRequestIndex: MutableStateFlow<Int> =
        0.toMutableStateFlowAsInitial()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val serverBudgets: StateFlow<Loadable<Result<List<SyncHandle.GetBudgets.Response.Budget>>>> =
        serverBudgetsRequestIndex
            .flatMapWithScope(scope) { attemptScope, _ ->
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

    data class ServerBudget(
        val peekHash: UpchainHash?,
        val info: BudgetInfo,
    )

    val items: StateFlow<Loadable<Result<NonEmptyList<Pair<BudgetId, SyncClientListItemModel>>?>>> =
        combineState(
            scope = scope,
            a = serverBudgets,
            b = dependencies.budgetsStorage.list,
        ) { serverBudgetsOrErrorOrLoading, local ->
            serverBudgetsOrErrorOrLoading.map { serverBudgetsOrError ->
                serverBudgetsOrError.map { serverBudgets ->
                    val serverBudgets: MutableMap<BudgetId, ServerBudget> = serverBudgets
                        .associate { budget ->
                            budget.id to ServerBudget(
                                peekHash = budget.peekHash,
                                info = budget.info,
                            )
                        }
                        .toMutableMap()
                    buildList {
                        addAll(
                            local.map { (id, localBudget) ->
                                val serverPeekHash = serverBudgets.remove(id)
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
                            serverBudgets
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
                    items?.map { (id, localOrServer: Ior<BudgetRepository, ServerBudget>) ->
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