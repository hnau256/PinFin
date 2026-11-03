package org.hnau.pinfin.projector.utils

import androidx.compose.runtime.Composable
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.rememberRun
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.hue
import org.hnau.pinfin.projector.utils.icon
import org.hnau.pinfin.projector.utils.title

@Composable
fun AccountInfo?.rememberEntityUiInfo(
    localization: Localization,
): EntityUiInfo = foldNullable(
    ifNull = {
        EntityUiInfo.rememberForNoItem(
            entityTypeName = localization.account,
        )
    },
    ifNotNull = { info ->
        info.rememberRun {
            EntityUiInfo(
                hue = hue,
                icon = icon?.image.foldNullable(
                    ifNull = { null },
                    ifNotNull = { icon ->
                        EntityUiInfo.Icon(
                            main = icon,
                        )
                    },
                ),
                title = title,
            )
        }
    },
)


val AccountInfo.chipInfo: ChipInfo
    get() = ChipInfo(
        hue = hue,
        leading = icon
            ?.image
            ?.let(Drawable::Vector),
        title = title,
    )
