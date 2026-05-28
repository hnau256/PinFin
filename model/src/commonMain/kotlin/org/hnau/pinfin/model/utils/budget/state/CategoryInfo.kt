package org.hnau.pinfin.model.utils.budget.state

import kotlinx.serialization.Serializable
import org.hnau.commons.kotlin.foldNullable
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


    override fun compareTo(
        other: CategoryInfo,
    ): Int = title.compareTo(
        other = other.title,
    )

    operator fun plus(
        config: CategoryConfig,
    ) = CategoryInfo(
        title = config.title ?: title,
        hue = config.hue ?: hue,
        icon = config.icon?.variant ?: icon,
    )

    companion object {

        fun create(
            id: CategoryId,
            config: CategoryConfig?,
        ): CategoryInfo = createDefault(
            id = id,
        ).let { default ->
            config.foldNullable(
                ifNull = { default },
                ifNotNull = { configNotNull -> default + configNotNull },
            )
        }

        fun createDefault(
            id: CategoryId,
        ): CategoryInfo = CategoryInfo(
            title = id.id,
            hue = ModelHue
                .calcDefault(id.id.hashCode())
                .let(Mapper.modelHueToHue.direct),
            icon = null,
        )
    }
}