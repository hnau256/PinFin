package org.hnau.pinfin.model.sync.client.budget.utils

import org.hnau.pinfin.model.utils.budget.upchain.Upchain
import org.hnau.pinfin.model.utils.budget.upchain.UpchainHash
import org.hnau.pinfin.model.utils.budget.upchain.Update

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