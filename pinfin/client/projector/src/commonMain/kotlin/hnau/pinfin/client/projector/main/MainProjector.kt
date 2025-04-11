package hnau.pinfin.client.projector.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.common.compose.uikit.bubble.BubblesShower
import hnau.common.compose.uikit.state.LoadableContent
import hnau.common.compose.uikit.state.NullableStateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.uikit.utils.appInsets
import hnau.common.compose.utils.Icon
import hnau.common.compose.utils.plus
import hnau.pinfin.client.data.budget.AccountInfoResolver
import hnau.pinfin.client.data.budget.CategoryInfoResolver
import hnau.pinfin.client.model.MainModel
import hnau.pinfin.client.projector.utils.AmountFormatter
import hnau.pinfin.client.projector.utils.DateTimeFormatter
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class MainProjector(
    private val scope: CoroutineScope,
    private val model: MainModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        //TODO remove
        val bubblesShower: BubblesShower

        val dateTimeFormatter: DateTimeFormatter

        val amountFormatter: AmountFormatter

        val accountInfoResolver: AccountInfoResolver

        val categoryInfoResolver: CategoryInfoResolver
    }

    @Composable
    fun Content() {
        val contentPadding = appInsets
        Transactions(
            contentPadding = contentPadding + PaddingValues(bottom = 96.dp)
        )
        AddTransactionButton(
            contentPadding = contentPadding,
        )
    }

    @Composable
    private fun Transactions(
        contentPadding: PaddingValues,
    ) {
        model
            .transactions
            .collectAsState()
            .value
            .LoadableContent(
                transitionSpec = TransitionSpec.crossfade(),
            ) { nonEmptyTransactionsOrNull ->
                nonEmptyTransactionsOrNull.NullableStateContent(
                    transitionSpec = TransitionSpec.crossfade(),
                    nullContent = {
                        //TODO()
                        Text("No transactions")
                    },
                ) { transactions ->
                    LazyColumn(
                        contentPadding = contentPadding,
                        verticalArrangement = Arrangement.spacedBy(Dimens.separation),
                    ) {
                        items(
                            items = transactions,
                            key = { (id) -> id.id },
                        ) { (id, transaction) ->
                            transaction.Content(
                                dependencies = dependencies,
                                onClick = { model.onEditTransactionClick(id, transaction) },
                            )
                        }
                    }
                }
            }
    }

    @Composable
    private fun AddTransactionButton(
        contentPadding: PaddingValues,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(Dimens.largeSeparation),
            contentAlignment = Alignment.BottomEnd,
        ) {
            FloatingActionButton(
                onClick = model.onAddTransactionClick,
            ) {
                Icon { Icons.Filled.Add }
            }
        }
    }
}