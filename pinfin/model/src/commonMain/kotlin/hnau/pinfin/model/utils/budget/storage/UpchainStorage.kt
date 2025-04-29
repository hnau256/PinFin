package hnau.pinfin.model.utils.budget.storage

import hnau.pinfin.model.utils.budget.upchain.Upchain
import kotlinx.coroutines.flow.StateFlow

interface UpchainStorage {

    val upchain: StateFlow<Upchain>

    suspend fun setNewUpchain(
        currentUpchainToCheck: Upchain,
        newUpchain: Upchain,
    ): Boolean
}