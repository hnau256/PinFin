package org.hnau.pinfin.model.sync.client.list

import arrow.core.Ior
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.LoadableStateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.combineState
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapReusable
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.map
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.sync.client.utils.TcpSyncClient
import org.hnau.pinfin.model.sync.utils.SyncHandle
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.BudgetInfo
import org.hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import org.hnau.pinfin.model.utils.budget.upchain.UpchainHash

class SyncClientListModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val tcpSyncClient: TcpSyncClient

        val budgetsStorage: BudgetsStorage

        fun item(): SyncClientListItemModel.Dependencies
    }


    private val serverBudgetsRequestIndex: MutableStateFlow<Int> =
        0.toMutableStateFlowAsInitial()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val serverBudgets: StateFlow<Loadable<Result<List<SyncHandle.GetBudgets.Response.Budget>>>> =
        serverBudgetsRequestIndex
            .flatMapWithScope(scope) { scope, _ ->
                LoadableStateFlow(scope) {
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
            first = serverBudgets,
            second = dependencies.budgetsStorage.list,
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

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}