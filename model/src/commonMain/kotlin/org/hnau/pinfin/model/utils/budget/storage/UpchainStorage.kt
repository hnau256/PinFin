package org.hnau.pinfin.model.utils.budget.storage

import kotlinx.coroutines.flow.StateFlow
import org.hnau.pinfin.model.utils.budget.upchain.Upchain

interface UpchainStorage {

    val upchain: StateFlow<Upchain>

    suspend fun setNewUpchain(
        currentUpchainToCheck: Upchain,
        newUpchain: Upchain,
    ): Boolean
}