package hnau.pinfin.projector.budget.analytics

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.toLazyListState
import hnau.pinfin.model.budget.analytics.tab.AccountsModel
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.accounts
import hnau.pinfin.projector.utils.AccountContent
import hnau.pinfin.projector.utils.AmountContent
import hnau.pinfin.projector.utils.formatter.AmountFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

class AccountsProjector(
    scope: CoroutineScope,
    private val model: AccountsModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val amountFormatter: AmountFormatter
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        val accountsOrNull = model
            .accounts
            .collectAsState()
            .value
        LazyColumn(
            modifier = Modifier.Companion.fillMaxSize(),
            state = model.scrollState.toLazyListState(),
            contentPadding = contentPadding,
        ) {
            accountsOrNull?.let { accounts ->
                stickyHeader(
                    key = "accounts",
                ) {
                    Title(
                        text = stringResource(Res.string.accounts)
                    )
                }
                items(
                    items = accounts,
                    key = { "account_" + it.id.id },
                ) { info ->
                    Account(
                        info = info,
                    )
                }
            }
        }
    }

    @Composable
    private fun Account(
        info: AccountInfo,
    ) {
        ListItem(
            headlineContent = {
                AccountContent(
                    info = info,
                )
            },
            trailingContent = {
                AmountContent(
                    value = info.amount,
                    amountFormatter = dependencies.amountFormatter,
                )
            },
            modifier = Modifier.Companion
                .fillMaxWidth()
                .clickable { model.onAccountClick(info) },
        )
    }

    @Composable
    private fun Title(
        text: String,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.Companion
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(
                    start = Dimens.separation,
                    end = Dimens.separation,
                    top = Dimens.separation,
                    bottom = Dimens.extraSmallSeparation,
                )
        )
    }

}