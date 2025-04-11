package hnau.common.compose.uikit.table.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import arrow.core.Either
import hnau.common.compose.uikit.table.TableOrientation

@Composable
fun TableLayout(
    orientation: TableOrientation,
    separationPx: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
        measurePolicy = remember(orientation, separationPx) {
            TableMeasurePolicy(
                orientation = orientation,
                separationPx = separationPx,
            )
        }
    )
}

private class TableMeasurePolicyWithoutIntrinsicSizes(
    private val orientation: TableOrientation,
    private val separationPx: Int,
) : MeasurePolicy {

    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val alongConstraintOrNull = constraints.maxAlong(
            orientation = orientation
        )

        val across = measurables
            .maxOf { measurable ->
                measurable.maxAcross(
                    orientation = orientation,
                    along = alongConstraintOrNull ?: Int.MAX_VALUE,
                )
            }
            .let { maxAcross ->
                constraints.constrainAcross(
                    orientation = orientation,
                    across = maxAcross,
                )
            }

        val info = MeasureInfo(
            orientation = orientation,
            across = across,
            constraints = Constraints.create(
                orientation = orientation,
                minAlong = constraints.minAlong(orientation) ?: 0,
                maxAlong = constraints.maxAlong(orientation) ?: Int.MAX_VALUE,
                minAcross = across,
                maxAcross = across,
            ),
            maxAlongOrNull = alongConstraintOrNull,
            separationPx = separationPx,
        )

        val weightSum = measurables.fold<Measurable, Float?>(
            initial = null,
        ) { acc, measurable ->
            measurable
                .tableParentData
                ?.weight
                ?.let { weight -> (acc ?: 0f) + weight }
                ?: acc
        }

        return when (weightSum) {
            null -> layoutWithoutWeight(
                measurables = measurables,
                info = info,
            )

            else -> layoutWithWeight(
                measurables = measurables,
                info = info,
                weightSum = weightSum,
            )
        }
    }

    private fun measure(
        measurable: Measurable,
        maxAlongOrNull: Int?,
        info: MeasureInfo,
        alongSum: Int,
        fillMaxParent: Boolean,
    ): Placeable {
        val maxAlong = maxAlongOrNull
            ?.minus(alongSum)
            ?.coerceAtLeast(0)

        val minAlong = when (fillMaxParent) {
            true -> info
                .constraints
                .minAlong(orientation = info.orientation)
                ?.minus(alongSum)
                ?.coerceAtLeast(0)
                ?: 0

            false -> 0
        }

        return measurable.measure(
            Constraints.create(
                orientation = info.orientation,
                minAlong = minAlong,
                maxAlong = maxAlong ?: Int.MAX_VALUE,
                minAcross = info.across,
                maxAcross = info.across,
            )
        )
    }

    private fun MeasureScope.layout(
        placeables: List<Placeable>,
        info: MeasureInfo,
        along: Int,
    ): MeasureResult = layout(
        orientation = info.orientation,
        along = info.constraints.constrainAlong(
            orientation = info.orientation,
            along = along,
        ),
        across = info.constraints.constrainAcross(
            orientation = info.orientation,
            across = info.across
        ),
    ) {
        var alongSum = 0
        placeables.forEachIndexed { i, placeable ->
            placeable.placeRelative(
                orientation = info.orientation,
                scope = this,
                along = alongSum,
                across = 0,
            )
            alongSum += placeable.along(
                orientation = info.orientation,
            )
            if (i < placeables.lastIndex) {
                alongSum += info.separationPx
            }
        }
    }

    private fun MeasureScope.layoutWithoutWeight(
        measurables: List<Measurable>,
        info: MeasureInfo,
    ): MeasureResult {
        val maxAlongOrNull = info.maxAlongOrNull
        var alongSum = 0
        val placeables = measurables.mapIndexed { i, measurable ->
            val placeable = measure(
                measurable = measurable,
                info = info,
                alongSum = alongSum,
                maxAlongOrNull = maxAlongOrNull,
                fillMaxParent = i == measurables.lastIndex,
            )
            alongSum += placeable.along(
                orientation = info.orientation,
            )
            if (i < measurables.lastIndex) {
                alongSum += info.separationPx
            }
            placeable
        }
        return layout(
            placeables = placeables,
            along = alongSum,
            info = info,
        )
    }


    private fun MeasureScope.layoutWithWeight(
        weightSum: Float,
        measurables: List<Measurable>,
        info: MeasureInfo,
    ): MeasureResult {
        val maxAlong = info
            .maxAlongOrNull
            ?: error("Unable use weight without constraint")

        var alongSum = 0

        val placeablesOrMeasurables: List<Either<Placeable, Pair<Measurable, Float>>> =
            measurables.mapIndexed { i, measurable ->
                @Suppress("MoveVariableDeclarationIntoWhen")
                val weight = measurable.tableParentData?.weight
                val result = when (weight) {
                    null -> {
                        val placeable = measure(
                            measurable = measurable,
                            info = info,
                            alongSum = alongSum,
                            maxAlongOrNull = maxAlong,
                            fillMaxParent = false,
                        )
                        alongSum += placeable.along(
                            orientation = info.orientation,
                        )
                        Either.Left(placeable)
                    }

                    else -> Either.Right(measurable to weight)
                }
                if (i < measurables.lastIndex) {
                    alongSum += info.separationPx
                }
                result
            }

        val alongToSplitBetweenItemsWithWeight = maxAlong - alongSum

        val placeables: List<Placeable> = placeablesOrMeasurables.map { placeableOrMeasurable ->
            when (placeableOrMeasurable) {
                is Either.Left -> placeableOrMeasurable.value
                is Either.Right -> {
                    val (measurable, weight) = placeableOrMeasurable.value
                    val along = (alongToSplitBetweenItemsWithWeight * weight / weightSum).toInt()
                        .coerceAtLeast(0)
                    measurable.measure(
                        constraints = Constraints.fixed(
                            orientation = info.orientation,
                            along = along,
                            across = info.across,
                        )
                    )
                }
            }
        }

        return layout(
            placeables = placeables,
            along = info.constraints.maxAlong(info.orientation) ?: Int.MAX_VALUE,
            info = info,
        )
    }

    private data class MeasureInfo(
        val orientation: TableOrientation,
        val across: Int,
        val constraints: Constraints,
        val maxAlongOrNull: Int?,
        val separationPx: Int,
    )
}

private class TableMeasurePolicy(
    private val orientation: TableOrientation,
    private val separationPx: Int,
) : MeasurePolicy by TableMeasurePolicyWithoutIntrinsicSizes(
    orientation = orientation,
    separationPx = separationPx,
) {

    private fun joinSizes(
        sizes: List<Int?>,
        sum: Boolean,
    ): Int {
        return when (sum) {
            true -> sizes
                .map { it ?: return Int.MAX_VALUE }
                .sum() + separationPx * (sizes.size - 1)

            false -> sizes.filterNotNull().maxOrNull() ?: 0
        }
    }

    private inline fun <R> List<IntrinsicMeasurable>.extractSizes(
        checkWeight: Boolean,
        extract: IntrinsicMeasurable.() -> R,
    ): List<R?> = map { measurable ->
        val hasWeight = checkWeight && measurable.tableParentData?.weight != null
        when (hasWeight) {
            true -> null
            false -> measurable.extract()
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int,
    ): Int {
        val isAlong = orientation == TableOrientation.Horizontal
        return joinSizes(
            sizes = measurables.extractSizes(checkWeight = isAlong) { maxIntrinsicWidth(height) },
            sum = isAlong
        )
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int,
    ): Int {
        val isAlong = orientation == TableOrientation.Horizontal
        return joinSizes(
            sizes = measurables.extractSizes(checkWeight = isAlong) { minIntrinsicWidth(height) },
            sum = isAlong
        )
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int,
    ): Int {
        val isAlong = orientation == TableOrientation.Vertical
        return joinSizes(
            sizes = measurables.extractSizes(checkWeight = isAlong) { maxIntrinsicHeight(width) },
            sum = isAlong
        )
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int,
    ): Int {
        val isAlong = orientation == TableOrientation.Vertical
        return joinSizes(
            sizes = measurables.extractSizes(checkWeight = isAlong) { minIntrinsicHeight(width) },
            sum = isAlong
        )
    }
}