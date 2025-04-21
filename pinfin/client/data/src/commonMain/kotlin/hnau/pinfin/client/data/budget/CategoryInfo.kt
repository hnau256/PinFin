package hnau.pinfin.client.data.budget

import hnau.common.kotlin.castOrNull
import hnau.pinfin.scheme.CategoryId
import kotlinx.serialization.Serializable

@Serializable
data class CategoryInfo(
    val id: CategoryId,
    val title: String = id.id,
) : Comparable<CategoryInfo> {


    override fun compareTo(
        other: CategoryInfo,
    ): Int = title.compareTo(
        other = other.title,
    )

    override fun equals(
        other: Any?,
    ): Boolean = other
        .castOrNull<CategoryInfo>()
        ?.id == id

    override fun hashCode(): Int = id.hashCode()
}