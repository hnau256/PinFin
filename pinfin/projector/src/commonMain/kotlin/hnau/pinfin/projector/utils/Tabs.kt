package hnau.pinfin.projector.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import kotlinx.collections.immutable.ImmutableList

@Composable
fun <T> Tabs(
    items: ImmutableList<T>,
    selected: T,
    onSelectedChanged: (T) -> Unit,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceAround,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    itemPaddingValues: PaddingValues = PaddingValues(
        horizontal = Dimens.separation,
        vertical = Dimens.smallSeparation,
    ),
    indicatorColor: Color = MaterialTheme.colorScheme.primaryContainer,
    indicatorContentColor: Color = contentColorFor(indicatorColor),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = contentColorFor(containerColor),
    padding: Dp = 3.dp,
    cornerRadius: Dp? = null,
    item: @Composable BoxScope.(item: T) -> Unit,
) {

    fun createShape(
        shrinkage: Dp,
    ): Shape = cornerRadius.foldNullable(
        ifNull = { RoundedCornerShape(100) },
        ifNotNull = { cornerRadius ->
            RoundedCornerShape(cornerRadius - shrinkage)
        }

    )

    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current

    val itemsSeparationPx = with(density) {
        horizontalArrangement.spacing.roundToPx()
    }

    val selectedIndexWithOffset: Float by animateFloatAsState(
        targetValue = items.indexOf(selected).toFloat(),
    )

    val indicatorShape = createShape(
        shrinkage = padding,
    )

    SubcomposeLayout(
        modifier = modifier
            .background(
                color = containerColor,
                shape = createShape(
                    shrinkage = 0.dp,
                )
            )
            .padding(padding),
    ) { constraints ->
        val tabs = items
            .map { item ->
                subcompose(
                    slotId = item,
                ) {
                    val isSelected = item == selected
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = isSelected
                            .foldBoolean(
                                ifTrue = { Modifier },
                                ifFalse = {
                                    Modifier
                                        .clip(indicatorShape)
                                        .clickable { onSelectedChanged(item) }
                                }
                            )
                            .padding(itemPaddingValues)
                    ) {
                        val contentColor = isSelected.foldBoolean(
                            ifTrue = { indicatorContentColor },
                            ifFalse = { contentColor },
                        )
                        CompositionLocalProvider(
                            LocalTextStyle provides MaterialTheme.typography.titleMedium,
                            LocalContentColor provides contentColor,
                        ) {
                            item(item)
                        }
                    }
                }[0]
            }
            .fold(
                initial = emptyList<Placeable>() to constraints.copy(minWidth = 0),
            ) { (acc, constraints), measurable ->
                val result: Placeable = measurable.measure(constraints)
                val newConstraints = constraints.offset(
                    horizontal = -(result.width + itemsSeparationPx),
                )
                (acc + result) to newConstraints
            }
            .first

        val itemsWidth = tabs.sumOf(Placeable::width)
        val separationsSum = (items.size - 1) * itemsSeparationPx
        val preferredWidth = itemsWidth + separationsSum

        val layoutWidth = constraints.constrainWidth(preferredWidth)

        val tabsMaxHeight = tabs.maxOfOrNull(Placeable::height) ?: 0
        val layoutHeight = constraints.constrainHeight(tabsMaxHeight)

        val xs = IntArray(tabs.size)
        with(horizontalArrangement) {
            density.arrange(
                totalSize = layoutWidth,
                sizes = tabs
                    .map { it.width }
                    .toIntArray(),
                layoutDirection = layoutDirection,
                outPositions = xs,
            )
        }

        val ys = tabs.map { tab ->
            verticalAlignment.align(
                size = tab.height,
                space = layoutHeight
            )
        }

        val indicatorRect = run {

            val indexFrom = selectedIndexWithOffset
                .fastRoundToInt()
                .coerceIn(0, items.lastIndex - 1)
            val indexTo = indexFrom + 1
            val fraction = selectedIndexWithOffset - indexFrom

            IntRect(
                offset = IntOffset(
                    x = lerp(
                        start = xs[indexFrom],
                        stop = xs[indexTo],
                        fraction = fraction,
                    ),
                    y = ys.minOrNull() ?: 0,
                ),
                size = IntSize(
                    width = lerp(
                        start = tabs[indexFrom].width,
                        stop = tabs[indexTo].width,
                        fraction = fraction,
                    ),
                    height = tabsMaxHeight,
                )
            )
        }

        val indicator = subcompose(
            slotId = "indicator",
        ) {
            Box(
                modifier = Modifier
                    .background(
                        shape = indicatorShape,
                        color = indicatorColor,
                    )
            )
        }[0]
            .measure(
                Constraints.fixed(
                    width = indicatorRect.size.width,
                    height = indicatorRect.size.height,
                )
            )

        layout(layoutWidth, layoutHeight) {
            indicator.place(indicatorRect.topLeft)
            tabs.fastForEachIndexed { i, tab ->
                tab.place(
                    x = xs[i],
                    y = ys[i],
                )
            }
        }
    }
}