package org.hnau.pinfin.projector.budget.analytics.graph.configured

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.SwitchHue
import org.hnau.commons.app.projector.utils.horizontalDisplayPadding
import org.hnau.commons.app.projector.utils.plus
import org.hnau.commons.app.projector.utils.toLazyListState
import org.hnau.commons.app.projector.utils.verticalDisplayPadding
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.Hue
import org.hnau.pinfin.model.budget.analytics.tab.graph.configured.GraphPageModel
import org.hnau.pinfin.model.utils.analytics.AnalyticsPage
import org.hnau.pinfin.model.utils.model
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.AccountContent
import org.hnau.pinfin.projector.utils.AmountContent
import org.hnau.pinfin.projector.utils.CategoryContent
import org.hnau.pinfin.projector.utils.ViewMode
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter


class GraphPageProjector(
    private val model: GraphPageModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val amountFormatter: AmountFormatter

        val localization: Localization
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
                transitionSpec = TransitionSpec.crossfade(),
            ) { stateOrNull ->
                stateOrNull?.let { state ->
                    State(
                        state = state,
                        contentPadding = contentPadding,
                    )
                }
            }
    }

    @Composable
    private fun State(
        state: GraphPageModel.State,
        contentPadding: PaddingValues,
    ) {
        LazyColumn(
            state = model.scrollState.toLazyListState(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding + PaddingValues(
                horizontal = Dimens.horizontalDisplayPadding,
                vertical = Dimens.verticalDisplayPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        ) {
            state.total?.let { total ->
                header(
                    key = "total_header",
                    title = { (dependencies.localization.total) },
                    amount = total,
                )
            }
            AmountDirection.entries.forEach { direction ->
                val half = state.values[direction] ?: return@forEach
                stateHalf(
                    direction = direction,
                    half = half,
                )
            }
        }
    }

    private fun LazyListScope.header(
        key: String,
        title: @Composable () -> String,
        amount: Amount,
    ) {
        stickyHeader(
            key = key,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(
                        top = Dimens.separation,
                        bottom = Dimens.smallSeparation,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    text = title(),
                )
                Spacer(Modifier.weight(1f))
                AmountContent(
                    amountFormatter = dependencies.amountFormatter,
                    value = amount,
                )
            }
        }
    }

    private fun LazyListScope.stateHalf(
        direction: AmountDirection,
        half: GraphPageModel.State.Half,
    ) {
        val keyPrefix = direction.name
        header(
            key = "${keyPrefix}_header",
            title = {
                when (direction) {
                    AmountDirection.Credit -> dependencies.localization.credits
                    AmountDirection.Debit -> dependencies.localization.debits
                }
            },
            amount = half.sum.withDirection(direction),
        )
        items(
            items = half.values,
            key = {
                when (val key = it.key) {
                    is AnalyticsPage.Item.Key.Account -> "${keyPrefix}_account_${key.account.id.id}"
                    is AnalyticsPage.Item.Key.Category -> "${keyPrefix}_category_${key.category?.id?.id}"
                    null -> "${keyPrefix}_null"
                }
            }
        ) { (key, value) ->
            Item(
                direction = direction,
                key = key,
                value = value,
                max = half.max,
            )
        }
    }

    @Composable
    private fun Item(
        direction: AmountDirection,
        key: AnalyticsPage.Item.Key?,
        value: GraphPageModel.State.Half.Value,
        max: Amount,
    ) {
        when (key) {
            is AnalyticsPage.Item.Key.Account -> Item(
                hue = key.account.hue,
                value = value,
                direction = direction,
                max = max,
            ) {
                AccountContent(
                    info = key.account,
                    localization = dependencies.localization,
                    viewMode = ViewMode.Full,
                )
            }

            is AnalyticsPage.Item.Key.Category -> Item(
                hue = key.category?.hue,
                value = value,
                direction = direction,
                max = max,
                title = {
                    CategoryContent(
                        info = key.category,
                        localization = dependencies.localization,
                        viewMode = ViewMode.Full,
                    )
                }
            )

            null -> Item(
                hue = null,
                value = value,
                direction = direction,
                max = max,
                title = null,
            )
        }
    }

    @Composable
    private fun Item(
        hue: Hue?,
        value: GraphPageModel.State.Half.Value,
        max: Amount,
        direction: AmountDirection,
        title: (@Composable () -> Unit)?,
    ) {
        hue.foldNullable(
            ifNull = {
                Item(
                    value = value,
                    max = max,
                    direction = direction,
                    title = title,
                )
            },
            ifNotNull = { hueNotNull ->
                SwitchHue(
                    hue = hueNotNull.model,
                ) {
                    Item(
                        value = value,
                        max = max,
                        direction = direction,
                        title = title,
                    )
                }
            }
        )
    }

    @Composable
    private fun Item(
        value: GraphPageModel.State.Half.Value,
        max: Amount,
        direction: AmountDirection,
        title: (@Composable () -> Unit)?,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    model
                        .transactionsOpener
                        .openTransactions(
                            filters = value.filters,
                        )
                },
            verticalArrangement = Arrangement.spacedBy(Dimens.extraSmallSeparation),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                title?.invoke()
                Spacer(Modifier.weight(1f))
                AmountContent(
                    value = value.amount.withDirection(direction),
                    amountFormatter = dependencies.amountFormatter,
                )
            }
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = {
                    max
                        .value
                        .floatValue(exactRequired = false)
                        .takeIf { max -> max > 0 }
                        ?.let { max -> value.amount.value.floatValue(exactRequired = false) / max }
                        ?: 0f
                }
            )
        }
    }
}