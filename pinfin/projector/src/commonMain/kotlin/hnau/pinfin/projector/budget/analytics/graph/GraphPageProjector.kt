package hnau.pinfin.projector.budget.analytics.graph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.SwitchHue
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.plus
import hnau.common.app.projector.utils.verticalDisplayPadding
import hnau.common.kotlin.foldNullable
import hnau.pinfin.data.Amount
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.data.Hue
import hnau.pinfin.model.budget.analytics.tab.graph.GraphPageModel
import hnau.pinfin.model.utils.analytics.AnalyticsPage
import hnau.pinfin.model.utils.model
import hnau.pinfin.projector.utils.AccountContent
import hnau.pinfin.projector.utils.AmountContent
import hnau.pinfin.projector.utils.CategoryContent
import hnau.pinfin.projector.utils.SwitchHueToAmountDirection
import hnau.pinfin.projector.utils.formatter.AmountFormatter
import hnau.pinfin.projector.utils.icon
import hnau.pipe.annotations.Pipe

class GraphPageProjector(
    private val model: GraphPageModel,
    private val dependencies: Dependencies
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding + PaddingValues(
                horizontal = Dimens.horizontalDisplayPadding,
                vertical = Dimens.verticalDisplayPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        ) {
            when (state) {
                is GraphPageModel.State.CreditAndDebit -> {
                    stateHalf(
                        direction = AmountDirection.Credit,
                        half = state.credit,
                    )
                    stateHalf(
                        direction = AmountDirection.Credit,
                        half = state.debit,
                    )
                }

                is GraphPageModel.State.CreditOnly -> stateHalf(
                    direction = AmountDirection.Credit,
                    half = state.credit,
                )

                is GraphPageModel.State.DebitOnly -> stateHalf(
                    direction = AmountDirection.Credit,
                    half = state.debit,
                )
            }
        }
    }

    private fun LazyListScope.stateHalf(
        direction: AmountDirection,
        half: GraphPageModel.State.Half,
    ) {
        items(
            items = half.values,
            key = {
                when (val key = it.key) {
                    is AnalyticsPage.Item.Key.Account -> "account_${key.account.id.id}"
                    is AnalyticsPage.Item.Key.Category -> "category_${key.category?.id?.id}"
                    null -> "null"
                }
            }
        ) { (key, amount) ->
            Item(
                direction = direction,
                key = key,
                amount = amount,
                max = half.max,
            )
        }
    }

    @Composable
    private fun Item(
        direction: AmountDirection,
        key: AnalyticsPage.Item.Key?,
        amount: Amount,
        max: Amount,
    ) {
        when (key) {
            is AnalyticsPage.Item.Key.Account -> Item(
                hue = key.account.hue,
                amount = amount,
                direction = direction,
                max = max,
            ) {
                AccountContent(
                    info = key.account,
                )
            }

            is AnalyticsPage.Item.Key.Category -> Item(
                hue = key.category?.hue,
                amount = amount,
                direction = direction,
                max = max,
                title = key.category?.let { category ->
                    {
                        CategoryContent(
                            info = category,
                        )
                    }
                }
            )

            null -> Item(
                hue = null,
                amount = amount,
                direction = direction,
                max = max,
                title = null,
            )
        }
    }

    @Composable
    private fun Item(
        hue: Hue?,
        amount: Amount,
        max: Amount,
        direction: AmountDirection,
        title: (@Composable () -> Unit)?,
    ) {
        hue.foldNullable(
            ifNull = {
                Item(
                    amount = amount,
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
                        amount = amount,
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
        amount: Amount,
        max: Amount,
        direction: AmountDirection,
        title: (@Composable () -> Unit)?,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.extraSmallSeparation),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                title?.invoke()
                Spacer(Modifier.weight(1f))
                AmountContent(
                    value = amount.withDirection(direction),
                    amountFormatter = dependencies.amountFormatter,
                )
            }
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = {
                    max
                        .value
                        .takeIf { max -> max > 0 }
                        ?.toFloat()
                        ?.let { max -> amount.value.toFloat() / max }
                        ?: 0f
                }
            )
        }
    }
}