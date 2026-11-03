package org.hnau.pinfin.projector.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.hnau.commons.app.model.theme.color.Contrast
import org.hnau.commons.app.projector.fractal.SIcon
import org.hnau.commons.app.projector.fractal.SLine
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.distance.LocalDistance
import org.hnau.commons.app.projector.fractal.size.units
import org.hnau.commons.app.projector.fractal.utils.container
import org.hnau.commons.app.projector.fractal.utils.content
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.SwitchHue
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.pinfin.data.Hue
import org.hnau.pinfin.model.utils.modelHueToHue

data class ChipInfo(
    val hue: Hue,
    val title: String,
    val leading: Drawable? = null,
    val trailing: Drawable? = null,
) {

    companion object {

        fun forNoItem(
            entityTypeName: String,
        ): ChipInfo = ChipInfo(
            hue = UIConstants.absentValueHue,
            trailing = Drawable.Vector(UIConstants.absentValueIcon),
            title = entityTypeName,
        )

        @Composable
        fun rememberForNoItem(
            entityTypeName: String,
        ): ChipInfo = remember(entityTypeName) {
            forNoItem(entityTypeName)
        }
    }
}

@Composable
fun Chip(
    info: ChipInfo,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    actionOrElseOrDisabled: ActionOrElse<Unit, *>? = null,
) {
    SwitchHue(
        hue = info.hue.let(Mapper.modelHueToHue.reverse),
    ) {
        SPanel(
            modifier = modifier,
            isSelected = selected,
            contentOrientation = Orientation.Horizontal,
            actionOrElseOrDisabled = actionOrElseOrDisabled,
        ) {
            SLine(
                orientation = Orientation.Horizontal,
                separation = LocalDistance.current.units.padding.along.small,
                acrossOrientation = Alignment.CenterHorizontally,
            ) {
                info.leading?.let { leading ->
                    SIcon(
                        drawable = leading,
                    )
                }
                SText(
                    text = info.title,
                    maxLines = 1,
                )
                info.trailing?.let { trailing ->
                    SIcon(
                        drawable = trailing,
                    )
                }
            }
        }
    }
}