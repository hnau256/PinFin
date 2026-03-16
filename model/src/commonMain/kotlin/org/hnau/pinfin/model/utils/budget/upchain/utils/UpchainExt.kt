package org.hnau.pinfin.model.utils.budget.upchain.utils

import org.hnau.pinfin.model.utils.budget.upchain.Upchain
import org.hnau.pinfin.model.utils.budget.upchain.UpchainHash
import org.hnau.pinfin.model.utils.budget.upchain.Update

fun Upchain.getUpdatesAfterHashIfPossible(
    hash: UpchainHash?,
): List<Update>? = when (hash) {
    null -> take(0)
    else -> indexesByHash[hash]?.let { indexOfHash -> take(indexOfHash + 1) }
}?.second