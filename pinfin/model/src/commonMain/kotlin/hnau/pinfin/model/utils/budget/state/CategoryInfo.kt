package hnau.pinfin.model.utils.budget.state

import hnau.pinfin.data.CategoryId
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
}