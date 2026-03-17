package org.hnau.pinfin.projector.budget.analytics

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
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.toLazyListState
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.budget.analytics.tab.AccountsModel
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.AccountContent
import org.hnau.pinfin.projector.utils.AmountContent
import org.hnau.pinfin.projector.utils.ViewMode
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter


class AccountsProjector(
    private val model: AccountsModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val amountFormatter: AmountFormatter

        val localization: Localization
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
            modifier = Modifier.fillMaxSize(),
            state = model.scrollState.toLazyListState(),
            contentPadding = contentPadding,
        ) {
            accountsOrNull?.let { accounts ->
                stickyHeader(
                    key = "accounts",
                ) {
                    Title(
                        text = (dependencies.localization.accounts)
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
                    localization = dependencies.localization,
                    viewMode = ViewMode.Full,
                )
            },
            trailingContent = {
                AmountContent(
                    value = info.amount,
                    amountFormatter = dependencies.amountFormatter,
                )
            },
            modifier = Modifier
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
            modifier = Modifier
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