package hnau.pinfin.sync.server

import hnau.pinfin.sync.common.ApiResponse
import hnau.pinfin.sync.common.SyncHandle
import hnau.pinfin.sync.common.map
import hnau.pinfin.upchain.BudgetsStorage
import kotlinx.coroutines.CoroutineScope

class ServerSyncApi(
    scope: CoroutineScope,
    budgetsStorage: BudgetsStorage,
) : SyncApi {

    private val syncServer = BudgetsSyncServer(
        scope = scope,
        budgetsStorage = budgetsStorage,
    )

    @Suppress("UNCHECKED_CAST")
    override suspend fun <O, I : SyncHandle<O>> handle(
        request: I,
    ): ApiResponse<O> = when (request) {
        SyncHandle.Ping ->
            ping(request) as ApiResponse<O>

        SyncHandle.GetBudgets ->
            getBudgets(request) as ApiResponse<O>

        is SyncHandle.CheckContainsOneOfHashes ->
            checkContainsOneOfHashes(request) as ApiResponse<O>

        is SyncHandle.GetUpdates ->
            getUpdates(request) as ApiResponse<O>

        is SyncHandle.AddUpdates ->
            addUpdates(request) as ApiResponse<O>
    }

    private fun ping(
        request: SyncHandle.Ping,
    ): ApiResponse<SyncHandle.Ping.Response> = ApiResponse.Success(
        data = SyncHandle.Ping.Response,
    )

    private suspend fun getBudgets(
        request: SyncHandle.GetBudgets,
    ): ApiResponse<SyncHandle.GetBudgets.Response> = syncServer
        .getBudgets()
        .map { budgets ->
            SyncHandle.GetBudgets.Response(
                budgets = budgets
                    .map { (id, peekHash) ->
                        SyncHandle.GetBudgets.Response.Budget(
                            id = id,
                            peekHash = peekHash,
                        )
                    }
            )
        }

    private suspend fun checkContainsOneOfHashes(
        request: SyncHandle.CheckContainsOneOfHashes,
    ): ApiResponse<SyncHandle.CheckContainsOneOfHashes.Response> = syncServer
        .checkContainsOneOfHashes(
            budgetId = request.budgetId,
            hashesToCheck = request.hashes,
        )
        .map { foundHash ->
            SyncHandle.CheckContainsOneOfHashes.Response(
                foundHash = foundHash,
            )
        }

    private suspend fun getUpdates(
        request: SyncHandle.GetUpdates,
    ): ApiResponse<SyncHandle.GetUpdates.Response> = syncServer
        .getUpdates(
            budgetId = request.budgetId,
            after = request.after,
        )
        .map { result ->
            SyncHandle.GetUpdates.Response(
                updates = result.updates,
                hasMoreUpdates = result.hasMoreUpdates,
            )
        }

    private suspend fun addUpdates(
        request: SyncHandle.AddUpdates,
    ): ApiResponse<SyncHandle.AddUpdates.Response> = syncServer
        .addUpdates(
            budgetId = request.budgetId,
            after = request.after,
            updates = request.updates,
        )
        .map { result ->
            SyncHandle.AddUpdates.Response
        }
}