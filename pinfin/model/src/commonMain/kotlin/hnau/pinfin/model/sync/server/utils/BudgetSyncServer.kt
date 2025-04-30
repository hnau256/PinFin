package hnau.pinfin.model.sync.server.utils

import arrow.core.raise.result
import hnau.pinfin.model.sync.client.budget.utils.RemoteUpchain.GetMaxToMinUpdatesResult
import hnau.pinfin.model.sync.utils.SyncHandle
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.storage.UpchainStorage
import hnau.pinfin.model.utils.budget.storage.update
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import hnau.pinfin.model.utils.budget.upchain.Update
import hnau.pinfin.model.utils.budget.upchain.plus
import hnau.pinfin.model.utils.budget.upchain.utils.UpchainSyncConstants
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BudgetSyncServer(
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        val budgetRepository: Deferred<BudgetRepository>
    }

    private val accessStateMutex: Mutex = Mutex()

    private suspend inline fun <R> withUpchain(
        block: (UpchainStorage) -> R,
    ): R = accessStateMutex.withLock {
        dependencies
            .budgetRepository
            .await()
            .upchainStorage
            .let(block)
    }

    suspend fun getPeekHash(): UpchainHash? = withUpchain { upchainStorage ->
        upchainStorage.upchain.value.peekHash
    }

    suspend fun getMaxToMinUpdates(
        before: UpchainHash?,
    ): Result<SyncHandle.GetMaxToMinUpdates.Response> = withUpchain { upchainStorage ->
        result {
            val upchain = upchainStorage.upchain.value
            val totalCount = upchain.items.size
            val beforeIndex = before?.let { beforeNotNull ->
                runCatching { upchain.indexesByHash.getValue(beforeNotNull) }.bind()
            }
            val dropLastCount = when (beforeIndex) {
                null -> 0
                else -> totalCount - beforeIndex
            }
            SyncHandle.GetMaxToMinUpdates.Response(
                updates = upchain
                    .items
                    .asReversed()
                    .asSequence()
                    .drop(dropLastCount)
                    .take(UpchainSyncConstants.updatesToSendPortionSize)
                    .toList(),
                hasMoreUpdates = totalCount > dropLastCount + UpchainSyncConstants.updatesToSendPortionSize
            )
        }
    }

    suspend fun appendUpdates(
        peekHashToCheck: UpchainHash?,
        updates: List<Update>,
    ): Result<Unit> = withUpchain { upchainStorage ->
        result {
            upchainStorage.update { currentUpchain ->
                if (currentUpchain.peekHash != peekHashToCheck) {
                    raise(IllegalStateException("Incorrect peek hash"))
                }
                currentUpchain + updates
            }
        }
    }
}