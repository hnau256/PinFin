package hnau.pinfin.projector.budget.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.model.budget.analytics.tab.CategoriesModel
import hnau.pinfin.projector.utils.AmountContent
import hnau.pinfin.projector.utils.category.CategoryContent
import hnau.pinfin.projector.utils.formatter.AmountFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlin.math.absoluteValue

class CategoriesProjector(
    scope: CoroutineScope,
    private val model: CategoriesModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val amountFormatter: AmountFormatter
    }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        model
            .state
            .collectAsState()
            .value
            .LoadableContent(
                modifier = Modifier.fillMaxSize(),
                transitionSpec = TransitionSpec.crossfade(),
            ) { stateOrNull ->
                stateOrNull?.let { state -> //TODO handle null
                    State(
                        state = state,
                        contentPadding = contentPadding,
                    )
                }
            }
    }

    @Composable
    private fun State(
        contentPadding: PaddingValues,
        state: CategoriesModel.State,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .horizontalDisplayPadding(),
            contentPadding = contentPadding,
        ) {
            item(
                key = "total",
            ) {
                Row(
                    modifier = Modifier.padding(
                        vertical = Dimens.separation,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                ) {
                    AmountContent(
                        amountFormatter = dependencies.amountFormatter,
                        value = state.sum,
                    )
                }
            }
            items(
                items = state.items,
                key = { "category_" + it.info.id.id },
            ) { item ->
                Category(
                    item = item,
                )
            }
        }
    }

    @Composable
    private fun Category(
        item: CategoriesModel.State.Item,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.extraSmallSeparation),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = Dimens.separation,
                ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
            ) {
                CategoryContent(
                    info = item.info,
                )
                Spacer(Modifier.weight(1f))
                AmountContent(
                    amountFormatter = dependencies.amountFormatter,
                    value = item.amount,
                )
            }
                LinearProgressIndicator(
                    progress = { item.fraction },
                    modifier = Modifier.fillMaxWidth(),
                    color = when (item.amount.direction) {
                        AmountDirection.Credit -> MaterialTheme.colorScheme.primary
                        AmountDirection.Debit -> MaterialTheme.colorScheme.error
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceContainer,
                )
        }
    }
}