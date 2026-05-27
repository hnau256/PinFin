package org.hnau.pinfin.model.utils.budget.state.prototype

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hnau.upchain.core.Upchain
import org.hnau.upchain.core.getUpdatesAfterHashIfPossible


suspend fun BudgetStatePrototype.withNewUpchain(
    newUpchain: Upchain,
): BudgetStatePrototype = withContext(Dispatchers.Default) {

    val additionalUpdates = newUpchain.getUpdatesAfterHashIfPossible(
        hash = hash,
    )
    val (state, updates) = when (additionalUpdates) {
        null -> BudgetStatePrototype.empty to newUpchain.items.map(Upchain.Item::update)

        else -> this@withNewUpchain to additionalUpdates
    }
    updates.fold(
        initial = state,
        operation = BudgetStatePrototype::plus,
    )
}