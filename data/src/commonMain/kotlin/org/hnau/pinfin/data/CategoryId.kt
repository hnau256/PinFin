package org.hnau.pinfin.data

import kotlinx.serialization.Serializable
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.mapper.Mapper
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class CategoryId private constructor(
    val id: String,
) : Comparable<CategoryId> {

    constructor(
        direction: AmountDirection,
        idSuffix: String,
    ) : this(
        id = "${prefixes[direction]}$idSuffix"
    )

    val title: String
        get() = id.drop(1)

    val direction: AmountDirection
        get() = id
            .startsWith(prefixes.debit)
            .foldBoolean(
                ifTrue = { AmountDirection.Debit },
                ifFalse = { AmountDirection.Credit }
            )

    override fun compareTo(other: CategoryId): Int =
        id.compareTo(other.id)

    companion object {

        private val prefixes: AmountDirectionValues<Char> = AmountDirectionValues(
            debit = '-',
            credit = '+',
        )

        @Suppress("DEPRECATION")
        val stringMapper: Mapper<String, CategoryId> = Mapper(
            direct = ::CategoryId,
            reverse = CategoryId::id,
        )
    }
}