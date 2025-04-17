package hnau.pinfin.client.projector.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.common.compose.uikit.ErrorPanel
import hnau.common.compose.uikit.bubble.BubblesShower
import hnau.common.compose.uikit.state.LoadableContent
import hnau.common.compose.uikit.state.NullableStateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.Icon
import hnau.common.compose.utils.NavigationIcon
import hnau.common.compose.utils.plus
import hnau.pinfin.client.data.budget.AccountInfoResolver
import hnau.pinfin.client.data.budget.CategoryInfoResolver
import hnau.pinfin.client.model.TransactionsModel
import hnau.pinfin.client.projector.utils.AmountFormatter
import hnau.pinfin.client.projector.utils.DateTimeFormatter
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource
import pinfin.pinfin.client.projector.generated.resources.Res
import pinfin.pinfin.client.projector.generated.resources.add_transaction
import pinfin.pinfin.client.projector.generated.resources.no_transactions

class TransactionsProjector(
    private val scope: CoroutineScope,
    private val model: TransactionsModel,
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Транзакции") },
                    navigationIcon = { model.globalGoBackHandler.NavigationIcon() },
                )
            },
        ) { contentPadding ->
            Transactions(
                contentPadding = contentPadding + PaddingValues(bottom = 96.dp)
            )
            AddTransactionButton(
                contentPadding = contentPadding,
            )
        }
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
                        ErrorPanel(
                            title = {
                                Text(stringResource(Res.string.no_transactions))
                            },
                            button = {
                                OutlinedButton(
                                    onClick = model.onAddTransactionClick,
                                ) {
                                    Text(stringResource(Res.string.add_transaction))
                                }
                            }
                        )
                    },
                ) { transactions ->
                    LazyColumn(
                        contentPadding = contentPadding + PaddingValues(vertical = Dimens.separation),
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