package org.hnau.pinfin.data

import kotlinx.serialization.Serializable
import org.hnau.commons.kotlin.mapper.Mapper
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class CategoryId(
    val id: String,
): Comparable<CategoryId> {

    override fun compareTo(other: CategoryId): Int =
        id.compareTo(other.id)

    companion object {

        @Suppress("DEPRECATION")
        val stringMapper: Mapper<String, CategoryId> = Mapper(
            direct = ::CategoryId,
            reverse = CategoryId::id,
        )
    }
}