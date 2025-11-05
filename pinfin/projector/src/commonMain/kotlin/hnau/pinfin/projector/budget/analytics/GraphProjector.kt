package hnau.pinfin.projector.budget.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import arrow.core.NonEmptyList
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.Overcompose
import hnau.common.app.projector.utils.SlideOrientation
import hnau.common.app.projector.utils.SwitchHue
import hnau.common.app.projector.utils.copy
import hnau.common.app.projector.utils.getTransitionSpecForSlide
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.rememberLet
import hnau.common.app.projector.utils.verticalDisplayPadding
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.map
import hnau.pinfin.data.Amount
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.data.Hue
import hnau.pinfin.model.budget.analytics.tab.GraphModel
import hnau.pinfin.model.utils.analytics.GraphProvider
import hnau.pinfin.model.utils.analytics.GroupKey
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pinfin.model.utils.model
import hnau.pinfin.projector.utils.AccountContent
import hnau.pinfin.projector.utils.AmountContent
import hnau.pinfin.projector.utils.CategoryContent
import hnau.pinfin.projector.utils.formatter.AmountFormatter
import hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.flow.StateFlow

class GraphProjector(
    private val model: GraphModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val amountFormatter: AmountFormatter

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
            Item(
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
        ItemOrEmptyOrLoading(
            itemOrEmptyOrLoading = itemOrEmptyOrLoading,
            modifier = modifier,
        ) { item ->
            val contentOrEmptyOrLoading by produceState<Loadable<GraphProvider.Item.Content?>>(
                initialValue = Loading,
            ) {
                value = item
                    .getContent()
                    .let(::Ready)
            }
            contentOrEmptyOrLoading
                .LoadableContent(
                    transitionSpec = TransitionSpec.crossfade(),
                ) { contentOrEmpty ->
                    contentOrEmpty?.let { content ->
                        ItemContent(
                            content = content,
                        )
                    }
                }
        }
    }

    @Composable
    private fun ItemContent(
        content: GraphProvider.Item.Content,
        modifier: Modifier = Modifier,
    ) {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(
                horizontal = Dimens.horizontalDisplayPadding,
                vertical = Dimens.verticalDisplayPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
        ) {
            items(
                items = content.values,
                key = { value ->
                    when (val key = value.key) {
                        is GroupKey.Account -> key.account.id.id.let { "account_$it" }

                        is GroupKey.Category -> key.category?.id?.id.foldNullable(
                            ifNull = { "no_category" },
                            ifNotNull = { "category_$it" }
                        )

                        null -> "no_key"
                    }
                }
            ) { value ->
                Value(
                    amountRange = content.amountsRange,
                    value = value,
                )
            }
        }
    }

    @Composable
    private fun Value(
        amountRange: ClosedRange<Amount>,
        value: GraphProvider.Item.Content.Value,
        modifier: Modifier = Modifier,
    ) {
        val transactions = value.transactions
        val amount = value.amount
        when (val key = value.key) {
            is GroupKey.Account -> Value(
                modifier = modifier,
                amountRange = amountRange,
                key = { AccountContent(key.account) },
                hue = key.account.hue,
                amount = amount,
                transactions = transactions,
            )

            is GroupKey.Category -> Value(
                modifier = modifier,
                amountRange = amountRange,
                key = key.category?.let { category ->
                    { CategoryContent(category) }
                },
                hue = key.category?.hue,
                amount = amount,
                transactions = transactions,
            )

            null -> Value(
                modifier = modifier,
                amountRange = amountRange,
                key = null,
                hue = null,
                amount = amount,
                transactions = transactions,
            )
        }
    }

    @Composable
    private fun Value(
        amountRange: ClosedRange<Amount>,
        key: (@Composable () -> Unit)?,
        hue: Hue?,
        amount: Amount,
        transactions: NonEmptyList<TransactionInfo>,
        modifier: Modifier = Modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
            modifier = modifier
                .clickable {
                    println(transactions)
                }
                .padding(
                    vertical = Dimens.separation,
                ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
            ) {
                key?.invoke()
                Spacer(Modifier.weight(1f))
                AmountContent(
                    amountFormatter = dependencies.amountFormatter,
                    value = amount,
                )
            }
            hue.foldNullable(
                ifNull = {
                    ValueContent(
                        amountRange = amountRange,
                        amount = amount,
                    )
                },
                ifNotNull = { hue ->
                    SwitchHue(
                        hue = hue.model,
                    ) {
                        ValueContent(
                            amountRange = amountRange,
                            amount = amount,
                        )
                    }
                }
            )

        }
    }

    @Composable
    private fun ValueContent(
        amountRange: ClosedRange<Amount>,
        amount: Amount,
        modifier: Modifier = Modifier,
    ) {

        val (direction, amount) = amount.splitToDirectionAndRaw()

        val (beforeWeight, afterWeight) = when (direction) {
            AmountDirection.Credit -> Pair(
                -amountRange.start,
                amountRange.endInclusive - amount
            )

            AmountDirection.Debit -> Pair(
                -amountRange.start - amount,
                amountRange.endInclusive,
            )
        }.map { amount ->
            amount.value.takeIf { it > 0 }?.toFloat()
        }

        Row(
            modifier = modifier,
        ) {
            beforeWeight?.let { Spacer(Modifier.weight(it)) }
            Box(
                modifier = Modifier
                    .weight(amount.value.toFloat())
                    .height(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(Dimens.cornerRadius),
                    )
            )
            afterWeight?.let { Spacer(Modifier.weight(it)) }
        }
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