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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.common.model.goback.GlobalGoBackHandler
import hnau.common.model.goback.GoBackHandler
import hnau.common.projector.uikit.ErrorPanel
import hnau.common.projector.uikit.bubble.BubblesShower
import hnau.common.projector.uikit.state.NullableStateContent
import hnau.common.projector.uikit.state.TransitionSpec
import hnau.common.projector.uikit.utils.Dimens
import hnau.common.projector.utils.Icon
import hnau.common.projector.utils.plus
import hnau.common.projector.utils.toLazyListState
import hnau.pinfin.model.budget.TransactionsModel
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.add_transaction
import hnau.pinfin.projector.no_transactions
import hnau.pinfin.projector.utils.formatter.AmountFormatter
import hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.ExperimentalUuidApi

class TransactionsProjector(
    private val scope: CoroutineScope,
    private val model: TransactionsModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        //TODO remove
        //val bubblesShower: BubblesShower

        val dateTimeFormatter: DateTimeFormatter

        val amountFormatter: AmountFormatter

        val globalGoBackHandler: GlobalGoBackHandler
    }

    private val globalGoBackHandler: GoBackHandler = dependencies
        .globalGoBackHandler
        .resolve(scope)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        Transactions(
            contentPadding = contentPadding + PaddingValues(bottom = 96.dp)
        )
        AddTransactionButton(
            contentPadding = contentPadding,
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    @Composable
    private fun Transactions(
        contentPadding: PaddingValues,
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
            ) { transactions ->
                LazyColumn(
                    contentPadding = contentPadding + PaddingValues(vertical = Dimens.separation),
                    verticalArrangement = Arrangement.spacedBy(Dimens.separation),
                    state = model.scrollState.toLazyListState(),
                ) {
                    items(
                        items = transactions,
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
                onClick = model::onAddTransactionClick,
            ) {
                Icon(Icons.Filled.Add)
            }
        }
    }
}