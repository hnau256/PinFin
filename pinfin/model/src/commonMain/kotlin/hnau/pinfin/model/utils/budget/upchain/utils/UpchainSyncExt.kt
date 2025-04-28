package hnau.pinfin.model.utils.budget.upchain.utils

import arrow.core.raise.result
import hnau.pinfin.model.utils.budget.upchain.utils.RemoteUpchain
import hnau.pinfin.model.utils.budget.upchain.Upchain
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import hnau.pinfin.model.utils.budget.upchain.Update

suspend fun Upchain.syncWithRemote(
    remote: RemoteUpchain,
): Result<Upchain> = result {

    var remoteHasMoreUpdates = true
    var minReceivedRemoteHash: UpchainHash? = null
    val remoteUpdatesBuffer: MutableList<Upchain.Item> = mutableListOf()

    var remotePeek: UpchainHash? = null
    val updatesToPush: MutableList<Update> = mutableListOf()

    suspend fun flushUpdates(): Result<Unit> = remote
        .appendUpdates(
            peekHashToCheck = remotePeek,
            updates = updatesToPush,
        )
        .onSuccess {
            updatesToPush.clear()
        }

    val result = merge(
        getNextMaxToMinOtherItem = {
            if (remoteUpdatesBuffer.isEmpty()) {
                if (remoteHasMoreUpdates) {
                    val getUpdatesResult = remote
                        .getMaxToMinUpdates(
                            before = minReceivedRemoteHash,
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