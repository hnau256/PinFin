package hnau.pinfin.model.utils.budget.state

import hnau.pinfin.data.CategoryConfig
import hnau.pinfin.data.CategoryId
import kotlinx.serialization.Serializable

@Serializable
data class CategoryInfo(
    val id: CategoryId,
    val title: String,
) : Comparable<CategoryInfo> {

    constructor(
        id: CategoryId,
        config: CategoryConfig?,
    ): this(
        id = id,
        title = config?.title ?: id.id,
    )


    override fun compareTo(
        other: CategoryInfo,
    ): Int = title.compareTo(
        other = other.title,
    )
}