package hnau.pinfin.model.sync.client.budget.utils

import hnau.pinfin.model.utils.budget.upchain.Upchain
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import hnau.pinfin.model.utils.budget.upchain.Update

interface RemoteUpchain {

    data class GetMaxToMinUpdatesResult(
        val updates: List<Upchain.Item>,
        val hasMoreUpdates: Boolean,
    )

    suspend fun getMaxToMinUpdates(
        before: UpchainHash?,
    ): Result<GetMaxToMinUpdatesResult>

    suspend fun appendUpdates(
        peekHashToCheck: UpchainHash?,
        updates: List<Update>,
    ): Result<Unit>
}