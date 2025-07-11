package hnau.pinfin.model.sync.server.utils

import arrow.core.identity
import arrow.core.raise.result
import hnau.common.kotlin.coroutines.mapListReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.foldNullable
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.sync.utils.SyncHandle
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.BudgetInfo
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import hnau.pinfin.model.utils.budget.upchain.Update
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

class BudgetsSyncServer(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val budgetsStorage: BudgetsStorage

        fun budgetSyncServer(
            upchain: BudgetRepository,
        ): BudgetSyncServer.Dependencies
    }

    private val budgetsUpdates: StateFlow<Map<BudgetId, BudgetSyncServer>> = dependencies
        .budgetsStorage
        .list
        .mapListReusable(
            scope = scope,
            extractKey = Pair<BudgetId, *>::first,
            transform = { budgetScope, (id, repository) ->
                val budgetSyncServer = BudgetSyncServer(
                    dependencies = dependencies.budgetSyncServer(
                        upchain = repository,
                    ),
                )
                id to budgetSyncServer
            }
        )
        .mapState(scope) { budgets ->
            budgets.associate(::identity)
        }



    data class Budget(
        val id: BudgetId,
        val peekHash: UpchainHash?,
        val info: BudgetInfo,
    )

    suspend fun getBudgets(): Result<List<Budget>> = result {
        coroutineScope {
            budgetsUpdates.value.let { servers ->
                servers
                    .mapValues { (_, server) ->
                        async { server.getBudget() }
                    }
                    .map { (id, deferredBudget) ->
                        val budget = deferredBudget.await()
                        Budget(
                            id = id,
                            peekHash = budget.peekHash,
                            info = budget.info,
                        )
                    }
            }
        }
    }

    suspend fun getMaxToMinUpdates(
        budgetId: BudgetId,
        before: UpchainHash?,
    ): Result<SyncHandle.GetMaxToMinUpdates.Response> = budgetsUpdates
        .value[budgetId]
        .foldNullable(
            ifNull = {
                Result.success(
                    SyncHandle.GetMaxToMinUpdates.Response(
                        updates = emptyList(),
                        hasMoreUpdates = false,
                    )
                )
            },
            ifNotNull = { budgetSyncServer ->
                budgetSyncServer.getMaxToMinUpdates(
                    before = before,
                )
            }
        )

    suspend fun appendUpdates(
        budgetId: BudgetId,
        peekHashToCheck: UpchainHash?,
        updates: List<Update>,
    ): Result<Unit> = run {
        dependencies.budgetsStorage.createNewBudgetIfNotExists(budgetId)
        budgetsUpdates.mapNotNull { it[budgetId] }.first()
    }.appendUpdates(
        peekHashToCheck = peekHashToCheck,
        updates = updates,
    )
}