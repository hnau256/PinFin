package hnau.pinfin.model.utils.budget.storage

import hnau.pinfin.model.utils.budget.upchain.Upchain
import hnau.pinfin.model.utils.budget.upchain.Update
import hnau.pinfin.model.utils.budget.upchain.plus

suspend fun UpchainStorage.update(
    block: (Upchain) -> Upchain,
) {
    while (true) {
        val current = upchain.value
        val new = block(current)
        if (setNewUpchain(
                currentUpchainToCheck = current,
                newUpchain = new,
            )
        ) {
            return
        }
    }
}

suspend fun UpchainStorage.addUpdates(
    updates: Iterable<Update>,
) {
    update { upchain -> upchain + updates }
}

suspend fun UpchainStorage.addUpdate(
    update: Update,
) {
    addUpdates(
        updates = listOf(update)
    )
}