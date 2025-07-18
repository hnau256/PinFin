package hnau.pinfin.model.utils.budget.state

import hnau.common.kotlin.ifNull
import hnau.pinfin.data.CategoryId
import kotlinx.serialization.Serializable

@Serializable
data class CategoryInfo(
    val id: CategoryId,
    val title: String = id.titleBasedOnId,
) : Comparable<CategoryInfo> {


    override fun compareTo(
        other: CategoryInfo,
    ): Int = id
        .direction
        .compareTo(other.id.direction)
        .takeIf { it != 0 }
        .ifNull {
            title.compareTo(
                other = other.title,
            )
        }
}