package org.hnau.pinfin.projector.utils

import androidx.compose.runtime.Composable
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.rememberRun
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.ifNull
import org.hnau.pinfin.model.utils.budget.state.CategoryIdWithInfo
import org.hnau.pinfin.projector.Localization

@Composable
fun CategoryIdWithInfo?.rememberEntityUiInfo(
    localization: Localization,
): EntityUiInfo = foldNullable(
    ifNull = {
        EntityUiInfo.rememberForNoItem(
            entityTypeName = localization.category,
        )
    },
    ifNotNull = { idWithInfo ->
        val directionIcon = idWithInfo.key.direction.icon
        idWithInfo.rememberRun(directionIcon) {
            EntityUiInfo(
                hue = value.hue,
                icon = value.icon?.image.foldNullable(
                    ifNull = {
                        EntityUiInfo.Icon(
                            main = directionIcon,
                        )
                    },
                    ifNotNull = { icon ->
                        EntityUiInfo.Icon(
                            main = icon,
                            additional = directionIcon,
                        )
                    }
                ),
                title = value.title,
            )
        }
    },
)


val CategoryIdWithInfo.chipInfo: ChipInfo
    get() {
        val directionIcon = key.direction.icon
        val icon = value.icon?.image
        return ChipInfo(
            hue = value.hue,
            leading = icon
                .ifNull { directionIcon }
                .let(Drawable::Vector),
            trailing = icon
                ?.let { Drawable.Vector(directionIcon) },
            title = value.title,
        )
    }