package hnau.pinfin.sync.server

import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.pinfin.sync.common.ApiResponse
import hnau.pinfin.sync.common.SyncApi
import hnau.pinfin.sync.common.SyncHandle
import hnau.pinfin.sync.common.map
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class ServerSyncApi(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) : SyncApi {

    @Shuffle
    interface Dependencies {

        fun budgetsSyncServer(): BudgetsSyncServer.Dependencies
    }

    @Serializable
    data class Skeleton(
        var budgets: BudgetsSyncServer.Skeleton? = null,
    )

    private val syncServer = BudgetsSyncServer(
        scope = scope,
        dependencies = dependencies.budgetsSyncServer(),
        skeleton = skeleton::budgets
            .toAccessor()
            .getOrInit { BudgetsSyncServer.Skeleton() },
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