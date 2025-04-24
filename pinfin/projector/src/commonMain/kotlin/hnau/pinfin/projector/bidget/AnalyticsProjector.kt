package hnau.pinfin.projector.bidget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import arrow.core.toNonEmptyListOrNull
import hnau.common.compose.uikit.utils.Dimens
import hnau.pinfin.data.repository.AccountInfo
import hnau.pinfin.model.budget.AnalyticsModel
import hnau.pinfin.projector.utils.AmountFormatter
import hnau.pinfin.projector.utils.SignedAmountContent
import hnau.pinfin.projector.utils.account.AccountContent
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource
import hnau.pinfin.projector.Res
import hnau.pinfin.projector.accounts

class AnalyticsProjector(
    scope: CoroutineScope,
    private val model: AnalyticsModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        val amountFormatter: AmountFormatter
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        val state by model.budgetState.collectAsState()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            state
                .accounts
                .toNonEmptyListOrNull()
                ?.let { accounts ->
                    stickyHeader(
                        key = "accounts",
                    ) {
                        Title(
                            text = stringResource(Res.string.accounts)
                        )
                    }
                    items(
                        accounts,
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
                SignedAmountContent(
                    amount = info.amount,
                    amountFormatter = dependencies.amountFormatter,
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
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