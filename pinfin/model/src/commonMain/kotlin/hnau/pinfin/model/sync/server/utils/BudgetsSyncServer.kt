package hnau.pinfin.model.sync.server.utils

import arrow.core.identity
import arrow.core.raise.result
import hnau.common.kotlin.coroutines.mapListReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.fold
import hnau.common.kotlin.ifNull
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.sync.utils.SyncHandle
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.repository.BudgetsRepository
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import hnau.pinfin.model.utils.budget.upchain.Update
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

class BudgetsSyncServer(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        val budgetsStorage: BudgetsRepository

        fun budgetSyncServer(
            upchain: Deferred<BudgetRepository>,
        ): BudgetSyncServer.Dependencies
    }

    private val budgetsUpdates: StateFlow<Map<BudgetId, BudgetSyncServer>> = dependencies
        .budgetsStorage
        .list
        .mapListReusable(
            scope = scope,
            extractKey = Pair<BudgetId, *>::first,
            transform = { budgetScope, (id, deferredRepository) ->
                val budgetSyncServer = BudgetSyncServer(
                    dependencies = dependencies.budgetSyncServer(
                        upchain = deferredRepository,
                    ),
                )
                id to budgetSyncServer
            }
        )
        .mapState(scope) { budgets ->
            budgets.associate(::identity)
        }


    suspend fun getBudgets(): Result<Map<BudgetId, UpchainHash?>> = result {
        coroutineScope {
            budgetsUpdates.value.let { servers ->
                servers
                    .mapValues { (_, server) ->
                        async { server.getPeekHash() }
                    }
                    .mapValues { (_, deferredPeek) ->
                        deferredPeek.await()
                    }
            }
        }
    }

    suspend fun getMaxToMinUpdates(
        budgetId: BudgetId,
        before: UpchainHash?,
    ): Result<SyncHandle.GetMaxToMinUpdates.Response> = budgetsUpdates
        .value[budgetId]
        .fold(
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
    ): Result<Unit> = budgetsUpdates
        .value[budgetId]
        .ifNull {
            dependencies.budgetsStorage.createNewBudget(budgetId)
            budgetsUpdates.mapNotNull { it[budgetId] }.first()
        }
        .appendUpdates(
            peekHashToCheck = peekHashToCheck,
            updates = updates,
        )
}