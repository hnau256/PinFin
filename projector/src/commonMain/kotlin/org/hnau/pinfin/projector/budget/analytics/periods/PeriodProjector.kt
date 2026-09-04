package org.hnau.pinfin.projector.budget.analytics.periods

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
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.Hue
import org.hnau.pinfin.data.fold
import org.hnau.pinfin.model.budget.analytics.tab.periods.PeriodModel
import org.hnau.pinfin.model.utils.analytics.GroupKey
import org.hnau.pinfin.model.utils.analytics.PeriodResult
import org.hnau.pinfin.model.utils.analytics.fold
import org.hnau.pinfin.model.utils.modelHueToHue
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.AccountContent
import org.hnau.pinfin.projector.utils.AmountContent
import org.hnau.pinfin.projector.utils.CategoryContent
import org.hnau.pinfin.projector.utils.ViewMode
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter

/**
 * Список сумм по группам за один период (аналог старого `GraphPageProjector`, см.
 * docs/analytics-v2-plan.md, "2.6. Структура кода") - работает поверх [PeriodModel]/[PeriodResult]
 * вместо `GraphPageModel`/`AnalyticsPage`.
 */
class PeriodProjector(
    private val model: PeriodModel,
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
            ) { result ->
                State(
                    result = result,
                    contentPadding = contentPadding,
                )
            }
    }

    @Composable
    private fun State(
        result: PeriodResult,
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
            result.total?.let { total ->
                header(
                    key = "total_header",
                    title = { dependencies.localization.total },
                    amount = total,
                )
            }
            AmountDirection.entries.forEach { direction ->
                val half = result.values[direction] ?: return@forEach
                resultHalf(
                    direction = direction,
                    half = half,
                )
            }
        }
    }

    private fun LazyListScope.header(
        key: String,
        title: @Composable () -> String,
        amount: KeyValue<AmountDirection, Amount>,
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

    private fun LazyListScope.resultHalf(
        direction: AmountDirection,
        half: PeriodResult.Half,
    ) {
        val keyPrefix = direction.name
        header(
            key = "${keyPrefix}_header",
            title = {
                direction.fold(
                    ifCredit = { dependencies.localization.credits },
                    ifDebit = { dependencies.localization.debits },
                )
            },
            amount = KeyValue(direction, half.sum),
        )
        items(
            items = half.values,
            key = { entry -> "${keyPrefix}_${groupItemKey(entry.key)}" },
        ) { (key, value) ->
            Item(
                direction = direction,
                key = key,
                value = value,
                max = half.max,
            )
        }
    }

    private fun groupItemKey(
        key: GroupKey,
    ): String = key.fold(
        ifNone = { "none" },
        ifCategory = { idWithCategory -> "category_${idWithCategory?.key?.id}" },
        ifAccount = { idWithAccount -> "account_${idWithAccount.key.id}" },
    )

    @Composable
    private fun Item(
        direction: AmountDirection,
        key: GroupKey,
        value: PeriodResult.Half.Value,
        max: Amount,
    ) {
        key.fold(
            ifNone = {
                Item(
                    hue = null,
                    value = value,
                    direction = direction,
                    max = max,
                    title = null,
                )
            },
            ifCategory = { idWithCategory ->
                Item(
                    hue = idWithCategory?.value?.hue,
                    value = value,
                    direction = direction,
                    max = max,
                    title = {
                        CategoryContent(
                            info = idWithCategory,
                            localization = dependencies.localization,
                            viewMode = ViewMode.Full,
                        )
                    },
                )
            },
            ifAccount = { idWithAccount ->
                Item(
                    hue = idWithAccount.value.hue,
                    value = value,
                    direction = direction,
                    max = max,
                    title = {
                        AccountContent(
                            info = idWithAccount.value,
                            localization = dependencies.localization,
                            viewMode = ViewMode.Full,
                        )
                    },
                )
            },
        )
    }

    @Composable
    private fun Item(
        hue: Hue?,
        value: PeriodResult.Half.Value,
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
                    hue = hueNotNull.let(Mapper.modelHueToHue.reverse),
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
        value: PeriodResult.Half.Value,
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
                    value = KeyValue(direction, value.amount),
                    amountFormatter = dependencies.amountFormatter,
                )
            }
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = {
                    max
                        .value
                        .floatValue(exactRequired = false)
                        .takeIf { maxValue -> maxValue > 0 }
                        ?.let { maxValue -> value.amount.value.floatValue(exactRequired = false) / maxValue }
                        ?: 0f
                }
            )
        }
    }
}
