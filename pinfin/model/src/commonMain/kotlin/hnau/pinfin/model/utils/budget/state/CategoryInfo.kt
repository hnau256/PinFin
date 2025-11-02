package hnau.pinfin.model.utils.budget.state

import hnau.common.app.model.utils.Hue as ModelHue
import hnau.pinfin.data.CategoryConfig
import hnau.pinfin.data.CategoryId
import hnau.pinfin.data.Hue
import hnau.pinfin.model.utils.icons.IconVariant
import hnau.pinfin.model.utils.icons.variant
import kotlinx.serialization.Serializable

@Serializable
data class CategoryInfo(
    val id: CategoryId,
    val title: String,
    val hue: Hue,
    val icon: IconVariant?,
) : Comparable<CategoryInfo> {

    constructor(
        id: CategoryId,
        config: CategoryConfig?,
    ): this(
        id = id,
        title = config?.title ?: id.id,
        hue = config?.hue ?: ModelHue.calcDefault(id.id.hashCode()).degrees.let(::Hue),
        icon = config?.icon?.variant,
    )


    override fun compareTo(
        other: CategoryInfo,
    ): Int = title.compareTo(
        other = other.title,
    )
}