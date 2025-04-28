package hnau.pinfin.model.utils.budget.upchain

import hnau.common.kotlin.castOrNull

class Upchain private constructor(
    val items: List<Item>,
    val indexesByHash: Map<UpchainHash, Int>,
) {

    data class Item(
        val update: Update,
        val hash: UpchainHash,
    )

    val peekHash: UpchainHash? =
        items.lastOrNull()?.hash


    operator fun plus(
        update: Update,
    ): Upchain {
        val newHash = peekHash + update
        return Upchain(
            items = items + Item(
                update = update,
                hash = newHash,
            ),
            indexesByHash = indexesByHash + (newHash to items.size),
        )
    }

    fun take(
        count: Int,
    ): Pair<Upchain, List<Update>> {
        val upchain = Upchain(
            items = items.take(count),
            indexesByHash = indexesByHash.filterValues { it < count },
        )
        val detachedUpdates = items
            .drop(count)
            .map(Item::update)
        return upchain to detachedUpdates
    }

    override fun equals(
        other: Any?,
    ): Boolean = other
        ?.castOrNull<Upchain>()
        ?.takeIf { it.peekHash == peekHash } != null

    override fun hashCode(): Int = peekHash
        ?.hashCode()
        ?: 0

    companion object {

        val empty = Upchain(
            items = emptyList(),
            indexesByHash = emptyMap(),
        )
    }
}