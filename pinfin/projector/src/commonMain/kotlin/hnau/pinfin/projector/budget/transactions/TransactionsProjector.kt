package hnau.pinfin.projector.budget.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hnau.pinfin.projector.utils.BackButtonWidth
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.projector.uikit.ErrorPanel
import hnau.common.app.projector.uikit.TopBarDefaults
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.plus
import hnau.common.app.projector.utils.toLazyListState
import hnau.pinfin.model.TransactionsModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.add_transaction
import hnau.pinfin.projector.resources.no_transactions
import hnau.pinfin.projector.utils.formatter.AmountFormatter
import hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.ExperimentalUuidApi

class TransactionsProjector(
    scope: CoroutineScope,
    private val model: TransactionsModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val dateTimeFormatter: DateTimeFormatter

        val amountFormatter: AmountFormatter

        val backButtonWidth: BackButtonWidth
    }

    @Composable
    fun Content(
        bottomInset: Dp,
        showAddButton: Boolean = true,
    ) {
        Transactions(
            bottomInset = bottomInset + 96.dp,
        )
        if (showAddButton) {
            AddTransactionButton(
                bottomInset = bottomInset,
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Composable
    private fun Transactions(
        bottomInset: Dp,
    ) {
        model
            .transactions
            .collectAsState()
            .value
            .NullableStateContent(
                transitionSpec = TransitionSpec.crossfade(),
                nullContent = {
                    ErrorPanel(
                        title = {
                            Text(stringResource(Res.string.no_transactions))
                        },
                        button = {
                            Button(
                                onClick = model::onAddTransactionClick,
                            ) {
                                Text(stringResource(Res.string.add_transaction))
                            }
                        }
                    )
                },
            ) { loadableTransactions ->
                loadableTransactions
                    .LoadableContent(
                        modifier = Modifier.fillMaxSize(),
                        transitionSpec = TransitionSpec.crossfade(),
                    ) { delayedTransactions ->
                        LazyColumn(
                            contentPadding = PaddingValues(
                                top = TopBarDefaults.height + Dimens.separation,
                                bottom = bottomInset + Dimens.separation,
                            ),
                            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
                            state = model.scrollState.toLazyListState(model::updateScrollState),
                        ) {
                            items(
                                items = delayedTransactions.value,
                                key = { it.id.id },
                            ) { info ->
                                info.Content(
                                    dependencies = dependencies,
                                    onClick = { model.onEditTransactionClick(info) },
                                )
                            }
                        }
                    }
            }
    }

    @Composable
    private fun AddTransactionButton(
        bottomInset: Dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomInset)
                .padding(Dimens.largeSeparation),
            contentAlignment = Alignment.BottomEnd,
        ) {
            FloatingActionButton(
                onClick = model::onAddTransactionClick,
            ) {
                Icon(Icons.Filled.Add)
            }
        }
    }
}