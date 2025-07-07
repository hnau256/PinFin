package hnau.pinfin.projector.budget.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.projector.uikit.state.LoadableContent
import hnau.common.projector.uikit.state.TransitionSpec
import hnau.common.projector.uikit.utils.Dimens
import hnau.common.projector.utils.horizontalDisplayPadding
import hnau.pinfin.data.Amount
import hnau.pinfin.model.budget.analytics.tab.CategoriesModel
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.projector.utils.AmountContent
import hnau.pinfin.projector.utils.SignedAmountContent
import hnau.pinfin.projector.utils.category.CategoryContent
import hnau.pinfin.projector.utils.formatter.AmountFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

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
                    horizontalArrangement = Arrangement.spacedBy(Dimens.separation),
                ) {
                    AmountContent(
                        sign = true,
                        amountFormatter = dependencies.amountFormatter,
                        value = state.creditSum,
                    )
                    AmountContent(
                        sign = false,
                        amountFormatter = dependencies.amountFormatter,
                        value = state.debitSum,
                    )
                    Text(
                        text = "=",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    SignedAmountContent(
                        amount = state.total,
                        amountFormatter = dependencies.amountFormatter,
                    )
                }
            }
            items(
                items = state.categories,
                key = { "category_" + it.first.id.id },
            ) { (info, amount) ->
                Category(
                    state = state,
                    info = info,
                    amount = amount,
                )
            }
        }
    }

    @Composable
    private fun Category(
        state: CategoriesModel.State,
        info: CategoryInfo,
        amount: Amount,
    ) {

    }
}