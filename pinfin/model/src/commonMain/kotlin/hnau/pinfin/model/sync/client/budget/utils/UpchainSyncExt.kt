package hnau.pinfin.model.sync.client.budget.utils

import arrow.core.raise.result
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.sync.client.utils.TcpSyncClient
import hnau.pinfin.model.sync.utils.SyncHandle
import hnau.pinfin.model.utils.budget.upchain.Upchain
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import hnau.pinfin.model.utils.budget.upchain.Update
import hnau.pinfin.model.utils.budget.upchain.utils.UpchainSyncConstants

suspend fun Upchain.syncWithRemote(
    budgetId: BudgetId,
    remote: TcpSyncClient,
): Result<Upchain> = result {

    var remoteHasMoreUpdates = true
    var minReceivedRemoteHash: UpchainHash? = null
    val remoteUpdatesBuffer: MutableList<Upchain.Item> = mutableListOf()

    var remotePeek: UpchainHash? = null
    val updatesToPush: MutableList<Update> = mutableListOf()

    suspend fun flushUpdates(): Result<Unit> = remote
        .handle(
            SyncHandle.AppendUpdates(
                budgetId = budgetId,
                peekHashToCheck = remotePeek,
                updates = updatesToPush,
            )
        )
        .map { }
        .onSuccess {
            updatesToPush.clear()
        }

    val result = merge(
        getNextMaxToMinOtherItem = {
            if (remoteUpdatesBuffer.isEmpty()) {
                if (remoteHasMoreUpdates) {
                    val getUpdatesResult = remote
                        .handle(
                            SyncHandle.GetMaxToMinUpdates(
                                budgetId = budgetId,
                                before = minReceivedRemoteHash,
                            )
                        )
                        .bind()
                    val updates = getUpdatesResult.updates
                    minReceivedRemoteHash = updates.lastOrNull()?.hash
                    remoteUpdatesBuffer.addAll(updates)
                    remoteHasMoreUpdates = getUpdatesResult.hasMoreUpdates
                }
            }
            remoteUpdatesBuffer.removeFirstOrNull()
        },
        addUpdateToOther = { update, previousPeek ->
            if (updatesToPush.isEmpty()) {
                remotePeek = previousPeek
            }
            updatesToPush += update
            if (updatesToPush.size >= UpchainSyncConstants.updatesToSendPortionSize) {
                flushUpdates().bind()
            }
        },
    )

    flushUpdates().bind()

    result
}