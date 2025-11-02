package hnau.pinfin.projector.budget.analytics

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.Overcompose
import hnau.common.app.projector.utils.SlideOrientation
import hnau.common.app.projector.utils.copy
import hnau.common.app.projector.utils.getTransitionSpecForSlide
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.rememberLet
import hnau.common.kotlin.Loadable
import hnau.pinfin.model.budget.analytics.tab.GraphModel
import hnau.pinfin.model.utils.analytics.GraphProvider
import hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.flow.StateFlow

class GraphProjector(
    private val model: GraphModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val dateTimeFormatter: DateTimeFormatter
    }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        val itemOrEmptyOrLoading: Loadable<GraphProvider.Item?> by model.item.collectAsState()
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
                        itemOrEmptyOrLoading = itemOrEmptyOrLoading,
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
            Period(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(contentPadding.copy(top = 0.dp, bottom = 0.dp)),
                itemOrEmptyOrLoading = itemOrEmptyOrLoading,
            )
        }
    }

    @Composable
    private fun Item(
        itemOrEmptyOrLoading: Loadable<GraphProvider.Item?>,
        modifier: Modifier = Modifier,
    ) {
    }

    @Composable
    private fun ItemOrEmptyOrLoading(
        itemOrEmptyOrLoading: Loadable<GraphProvider.Item?>,
        modifier: Modifier = Modifier,
        content: @Composable (GraphProvider.Item) -> Unit,
    ) {
        itemOrEmptyOrLoading
            .LoadableContent(
                modifier = modifier,
                transitionSpec = TransitionSpec.both(),
            ) { itemOrEmpty ->
                itemOrEmpty
                    .NullableStateContent(
                        transitionSpec = TransitionSpec.both(),
                        label = "ItemOrEmpty",
                    ) { item ->
                        item
                            .StateContent(
                                transitionSpec = getTransitionSpecForSlide(
                                    orientation = SlideOrientation.Horizontal,
                                ) {
                                    when (targetState.period.start > initialState.period.start) {
                                        true -> 1f
                                        false -> -1f
                                    } * 0.25f
                                },
                                contentKey = { it.period },
                                label = "Period",
                            ) { item ->
                                content(item)
                            }
                    }
            }
    }

    @Composable
    private fun Period(
        itemOrEmptyOrLoading: Loadable<GraphProvider.Item?>,
        modifier: Modifier = Modifier,
    ) {
        val formatter = dependencies.dateTimeFormatter
        ItemOrEmptyOrLoading(
            itemOrEmptyOrLoading = itemOrEmptyOrLoading,
            modifier = modifier,
        ) { item ->
            Text(
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                textAlign = TextAlign.Center,
                text = item
                    .period
                    .rememberLet(formatter) { period ->
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