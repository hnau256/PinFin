package hnau.pinfin.model.sync.server.utils

import hnau.pinfin.model.sync.utils.SyncApi
import hnau.pinfin.model.sync.utils.SyncHandle
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class ServerSyncApi(
    scope: CoroutineScope,
    dependencies: Dependencies,
) : SyncApi {

    @Shuffle
    interface Dependencies {

        fun budgetsSyncServer(): BudgetsSyncServer.Dependencies
    }

    private val syncServer = BudgetsSyncServer(
        scope = scope,
        dependencies = dependencies.budgetsSyncServer(),
    )

    @Suppress("UNCHECKED_CAST")
    override suspend fun <O, I : SyncHandle<O>> handle(
        request: I,
    ): Result<O> = when (request) {

        SyncHandle.GetBudgets ->
            getBudgets(request) as Result<O>

        is SyncHandle.GetMaxToMinUpdates ->
            getMaxToMinUpdates(request) as Result<O>

        is SyncHandle.AppendUpdates ->
            appendUpdates(request) as Result<O>
    }

    private suspend fun getBudgets(
        request: SyncHandle.GetBudgets,
    ): Result<SyncHandle.GetBudgets.Response> = syncServer
        .getBudgets()
        .map { budgets ->
            SyncHandle.GetBudgets.Response(
                budgets = budgets
                    .map { budget ->
                        SyncHandle.GetBudgets.Response.Budget(
                            id = budget.id,
                            peekHash = budget.peekHash,
                            info = budget.info,
                        )
                    }
            )
        }

    private suspend fun getMaxToMinUpdates(
        request: SyncHandle.GetMaxToMinUpdates,
    ): Result<SyncHandle.GetMaxToMinUpdates.Response> = syncServer
        .getMaxToMinUpdates(
            budgetId = request.budgetId,
            before = request.before,
        )

    private suspend fun appendUpdates(
        request: SyncHandle.AppendUpdates,
    ): Result<SyncHandle.AppendUpdates.Response> = syncServer
        .appendUpdates(
            budgetId = request.budgetId,
            peekHashToCheck = request.peekHashToCheck,
            updates = request.updates,
        )
        .map { result ->
            SyncHandle.AppendUpdates.Response
        }
}