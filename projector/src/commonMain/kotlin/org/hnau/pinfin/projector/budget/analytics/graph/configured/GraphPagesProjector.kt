package org.hnau.pinfin.projector.budget.analytics.graph.configured

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDateRange
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.Overcompose
import org.hnau.commons.app.projector.utils.SlideOrientation
import org.hnau.commons.app.projector.utils.copy
import org.hnau.commons.app.projector.utils.getTransitionSpecForSlide
import org.hnau.commons.app.projector.utils.horizontalDisplayPadding
import org.hnau.commons.app.projector.utils.plus
import org.hnau.commons.app.projector.utils.rememberLet
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.map
import org.hnau.pinfin.model.budget.analytics.tab.graph.configured.GraphPagesModel
import org.hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

class GraphPagesProjector(
    scope: CoroutineScope,
    private val model: GraphPagesModel,
    private val dependencies: Dependencies
) {

    @Pipe
    interface Dependencies {


        val dateTimeFormatter: DateTimeFormatter

        fun page(): GraphPageProjector.Dependencies
    }

    private val page: StateFlow<IndexedValue<KeyValue<LocalDateRange, GraphPageProjector>>> = model
        .pageWithIndex
        .mapState(scope) { indexedPage ->
            indexedPage.map { page ->
                KeyValue(
                    key = page.period,
                    value = GraphPageProjector(
                        model = page,
                        dependencies = dependencies.page(),
                    )
                )
            }
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        val indexedPeriodWithProjector by page.collectAsState()
        Overcompose(
            modifier = Modifier.fillMaxSize(),
            top = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            contentPadding.copy(bottom = 0.dp),
                        )
                        .horizontalDisplayPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                ) {
                    NavigateIcon(
                        onClick = model.switchToPrevious,
                        icon = Icons.Default.ChevronLeft,
                    )
                    Period(
                        modifier = Modifier.weight(1f),
                        period = indexedPeriodWithProjector.map(KeyValue<LocalDateRange, *>::key),
                    )
                    NavigateIcon(
                        onClick = model.switchToNext,
                        icon = Icons.Default.ChevronRight,
                    )
                }
            },
            bottom = {
                Spacer(
                    modifier = Modifier.height(contentPadding.calculateBottomPadding())
                )
            }
        ) { padding ->
            indexedPeriodWithProjector
                .map(KeyValue<*, GraphPageProjector>::value)
                .StateContent(
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = getTransitionSpecForSlide(
                        orientation = SlideOrientation.Horizontal,
                        duration = 0.5.seconds,
                    ) {
                        when (targetState.index > initialState.index) {
                            true -> 1f
                            false -> -1f
                        } * 0.25f
                    },
                    label = "Page",
                    contentKey = IndexedValue<*>::index,
                ) { (_, page) ->
                    page.Content(
                        contentPadding.copy(
                            top = 0.dp,
                            bottom = 0.dp,
                        ) + padding
                    )
                }
        }
    }


    @Composable
    private fun Period(
        period: IndexedValue<LocalDateRange>,
        modifier: Modifier = Modifier,
    ) {
        val formatter = dependencies.dateTimeFormatter
        period.StateContent(
            modifier = modifier,
            transitionSpec = getTransitionSpecForSlide(
                orientation = SlideOrientation.Horizontal,
            ) {
                when (targetState.index > initialState.index) {
                    true -> 1f
                    false -> -1f
                } * 0.5f
            },
            label = "Period",
            contentKey = IndexedValue<*>::index,
        ) { (_, period) ->
            Text(
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                textAlign = TextAlign.Center,
                text = period.rememberLet(formatter) { period ->
                    listOf(
                        period.start,
                        period.endInclusive,
                    ).joinToString(
                        separator = " - ",
                        transform = formatter::formatDate,
                    )
                }
            )
        }
    }

    @Composable
    private fun NavigateIcon(
        onClick: StateFlow<(() -> Unit)?>,
        icon: ImageVector,
    ) {
        val onClickOrNull by onClick.collectAsState()
        IconButton(
            onClick = { onClickOrNull?.invoke() },
            enabled = onClickOrNull != null,
        ) {
            Icon(
                icon = icon,
            )
        }
    }
}