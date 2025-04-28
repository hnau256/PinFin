package hnau.pinfin.model.utils.budget.upchain.utils

import hnau.pinfin.model.utils.budget.upchain.Upchain
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import hnau.pinfin.model.utils.budget.upchain.Update

fun Upchain.getUpdatesAfterHashIfPossible(
    hash: UpchainHash?,
): List<Update>? = when (hash) {
    null -> take(0)
    else -> indexesByHash[hash]?.let { indexOfHash -> take(indexOfHash + 1) }
}?.second