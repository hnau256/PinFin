package hnau.pinfin.data

import hnau.common.kotlin.mapper.Mapper
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class CategoryId(
    val id: String,
): Comparable<CategoryId> {

    constructor(
        direction: AmountDirection,
        title: String,
    ): this(
        id = directionPrefixes[direction] + title,
    )

    override fun compareTo(other: CategoryId): Int =
        id.compareTo(other.id)

    val direction: AmountDirection
        get() = id
            .firstOrNull()
            ?.let(directionByPrefix::get)
            ?: error("Unable resolve direction of category with id $id")

    val titleBasedOnId: String
        get() = id.drop(1)

    companion object {

        val directionPrefixes: CategoryDirectionValues<Char> = CategoryDirectionValues(
            credit = '+',
            debit = '-',
        )

        private val directionByPrefix: Map<Char, AmountDirection> = AmountDirection
            .entries
            .associateBy(directionPrefixes::get)

        @Suppress("DEPRECATION")
        val stringMapper: Mapper<String, CategoryId> = Mapper(
            direct = ::CategoryId,
            reverse = CategoryId::id,
        )
    }
}