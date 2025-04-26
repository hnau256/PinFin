package hnau.pinfin.sync.server

import hnau.pinfin.sync.common.ApiResponse
import hnau.pinfin.sync.common.UpchainHash
import hnau.pinfin.sync.common.UpchainState
import hnau.pinfin.upchain.BudgetUpchain
import hnau.pinfin.upchain.Update
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BudgetSyncServer(
    scope: CoroutineScope,
    private val upchain: BudgetUpchain,
) {

    private var deferredState: Deferred<UpchainState> = scope.async {
        upchain.useUpdates { updates ->
            updates.fold(
                initial = UpchainState.empty,
            ) { state, update ->
                state + update
            }
        }
    }

    suspend fun getPeekHash(): UpchainHash =
        withState { state, _ -> state.peekHash }

    suspend fun checkContainsOneOfHashes(
        hashesToCheck: List<UpchainHash>,
    ): UpchainHash? = withState { state, _ ->
        hashesToCheck.forEach { hashToCheck ->
            val contains = state.getHashIndex(hashToCheck) != null
            if (contains) {
                return@withState hashToCheck
            }
        }
        null
    }

    data class GetUpdatesResult(
        val updates: List<Update>,
        val hasMoreUpdates: Boolean,
    )

    suspend fun getUpdates(
        after: UpchainHash,
    ): ApiResponse<GetUpdatesResult> = withState { state, _ ->
        val indexOfHash = state.getHashIndex(after)
            ?: return@withState ApiResponse.Error("Unknown hash $after")
        val tail = state
            .updatesWithHashes
            .drop(indexOfHash + 1)
        val data = GetUpdatesResult(
            updates = tail.take(getUpdatesLimit).map(Pair<Update, *>::first),
            hasMoreUpdates = tail.size > getUpdatesLimit,
        )
        ApiResponse.Success(data)
    }

    suspend fun addUpdates(
        after: UpchainHash,
        updates: List<Update>,
    ): ApiResponse<Unit> = withState { state, updateState ->
        if (state.peekHash != after) {
            return@withState ApiResponse.Error("$after is not last upchain hash of bidget")
        }
        upchain.addUpdates(updates)
        val stateWithUpdates = updates.fold(
            initial = state,
        ) { state, update ->
            state + update
        }
        updateState(stateWithUpdates)
        ApiResponse.Success(Unit)
    }

    private val accessStateMutex = Mutex()

    private suspend inline fun <R> withState(
        block: (state: UpchainState, updateState: (UpchainState) -> Unit) -> R,
    ): R = accessStateMutex.withLock {
        val state = deferredState.await()
        block(
            state
        ) { newState ->
            deferredState = CompletableDeferred(newState)
        }
    }

    companion object {

        private const val getUpdatesLimit: Int = 1024
    }
}