package hnau.pinfin.model.utils.budget.upchain

import hnau.common.kotlin.castOrNull
import kotlinx.serialization.Serializable

class Upchain private constructor(
    val items: List<Item>,
    val indexesByHash: Map<UpchainHash, Int>,
    private val sha256: Sha256,
) {

    @Serializable
    data class Item(
        val update: Update,
        val hash: UpchainHash,
    )

    val peekHash: UpchainHash? =
        items.lastOrNull()?.hash


    operator fun plus(
        update: Update,
    ): Upchain {
        val newHash = peekHash.calcNext(
            update = update,
            sha256 = sha256,
        )
        return Upchain(
            items = items + Item(
                update = update,
                hash = newHash,
            ),
            indexesByHash = indexesByHash + (newHash to items.size),
            sha256 = sha256,
        )
    }

    fun take(
        count: Int,
    ): Pair<Upchain, List<Update>> {
        val upchain = Upchain(
            items = items.take(count),
            indexesByHash = indexesByHash.filterValues { it < count },
            sha256 = sha256,
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

        fun empty(
            sha256: Sha256,
        ): Upchain = Upchain(
            items = emptyList(),
            indexesByHash = emptyMap(),
            sha256 = sha256,
        )
    }
}