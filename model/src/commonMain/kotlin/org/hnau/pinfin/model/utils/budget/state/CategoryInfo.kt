package org.hnau.pinfin.model.utils.budget.state

import kotlinx.serialization.Serializable
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.pinfin.data.CategoryConfig
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Hue
import org.hnau.pinfin.model.utils.icons.IconVariant
import org.hnau.pinfin.model.utils.icons.variant
import org.hnau.pinfin.model.utils.modelHueToHue
import org.hnau.commons.app.model.theme.color.Hue as ModelHue

@Serializable
data class CategoryInfo(
    val title: String,
    val hue: Hue,
    val icon: IconVariant?,
) : Comparable<CategoryInfo> {

    constructor(
        id: CategoryId,
        config: CategoryConfig?,
    ) : this(
        title = config?.title ?: id.id,
        hue = config?.hue ?: ModelHue
            .calcDefault(id.id.hashCode())
            .let(Mapper.modelHueToHue.direct),
        icon = config?.icon?.variant,
    )


    override fun compareTo(
        other: CategoryInfo,
    ): Int = title.compareTo(
        other = other.title,
    )
}