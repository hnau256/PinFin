package hnau.pinfin.model.sync.utils

import hnau.pinfin.upchain.Update

class UpchainState(
    val updatesWithHashes: List<Pair<Update, UpchainHash>>,
    private val hashesIndex: Map<UpchainHash, Int>,
) {

    fun getHashIndex(
        hash: UpchainHash,
    ): Int? = when (hash) {
        UpchainHash.empty -> -1
        else -> hashesIndex[hash]
    }

    val peekHash: UpchainHash
        get()= updatesWithHashes
        .lastOrNull()
        ?.second
        ?: UpchainHash.empty

    operator fun plus(
        update: Update,
    ): UpchainState {
        val nextHash = peekHash + update
        return UpchainState(
            updatesWithHashes = updatesWithHashes + (update to nextHash),
            hashesIndex = hashesIndex + (nextHash to updatesWithHashes.size),
        )
    }

    companion object {

        val empty = UpchainState(
            updatesWithHashes = emptyList(),
            hashesIndex = emptyMap(),
        )
    }
}