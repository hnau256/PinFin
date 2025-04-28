package hnau.pinfin.model.utils.budget.storage

import hnau.pinfin.model.utils.budget.upchain.Update

suspend fun UpchainStorage.addUpdate(
    update: Update,
) {
    setNewUpchain(upchain.value + update)
}