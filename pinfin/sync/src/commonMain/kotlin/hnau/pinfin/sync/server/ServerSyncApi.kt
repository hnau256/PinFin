package hnau.pinfin.sync.server

import hnau.pinfin.sync.common.SyncApi
import hnau.pinfin.sync.common.SyncHandle
import hnau.pinfin.upchain.BudgetsStorage
import kotlinx.coroutines.CoroutineScope

class ServerSyncApi(
    scope: CoroutineScope,
    budgetsStorage: BudgetsStorage,
) : SyncApi {

    @Suppress("UNCHECKED_CAST")
    override suspend fun <O, I : SyncHandle<O>> handle(
        request: I,
    ): Result<O> = when (request) {
        SyncHandle.Ping -> handlePing(request) as Result<O>
    }

    private fun handlePing(
        request: SyncHandle.Ping,
    ): Result<SyncHandle.Ping.Response> = Result.success(
        value = SyncHandle.Ping.Response,
    )
}