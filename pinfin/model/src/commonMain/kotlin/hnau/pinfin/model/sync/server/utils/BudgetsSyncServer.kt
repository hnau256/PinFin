package hnau.pinfin.model.sync.server.utils

import arrow.core.identity
import arrow.core.raise.result
import hnau.common.kotlin.coroutines.mapListReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.repository.BudgetsRepository
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import hnau.pinfin.model.utils.budget.storage.UpchainStorage
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import hnau.pinfin.model.utils.budget.upchain.Update
import hnau.pinfin.model.utils.budget.upchain.utils.RemoteUpchain.GetMaxToMinUpdatesResult
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow

class BudgetsSyncServer(
    scope: CoroutineScope,
    dependencies: Dependencies,
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
    ): Result<GetMaxToMinUpdatesResult> = withBudget(
        budgetId = budgetId,
    ) { budgetSyncServer ->
        budgetSyncServer.getMaxToMinUpdates(
            before = before,
        )
    }

    suspend fun appendUpdates(
        budgetId: BudgetId,
        peekHashToCheck: UpchainHash?,
        updates: List<Update>,
    ): Result<Unit> = withBudget(
        budgetId = budgetId,
    ) { budgetSyncServer ->
        budgetSyncServer.appendUpdates(
            peekHashToCheck = peekHashToCheck,
            updates = updates,
        )
    }

    private inline fun <R> withBudget(
        budgetId: BudgetId,
        block: (BudgetSyncServer) -> Result<R>,
    ): Result<R> {
        val budget: BudgetSyncServer? = budgetsUpdates.value[budgetId]
        return when (budget) {
            null -> Result.failure(Throwable("Has no budget with id $budgetId"))
            else -> block(budget)
        }
    }
}