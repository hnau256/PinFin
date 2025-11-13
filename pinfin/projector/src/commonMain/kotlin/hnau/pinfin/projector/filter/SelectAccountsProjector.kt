package hnau.pinfin.projector.filter

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import arrow.core.NonEmptyList
import hnau.common.app.projector.uikit.row.ChipsFlowRow
import hnau.common.app.projector.utils.collectAsMutableAccessor
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.filter.pageable.SelectAccountsModel
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.accounts
import hnau.pinfin.projector.utils.AccountContent
import hnau.pinfin.projector.utils.Label
import org.jetbrains.compose.resources.stringResource

class SelectAccountsProjector(
    private val model: SelectAccountsModel,
) {

    class Page(
        private val model: SelectAccountsModel.Page,
    ) {

        @Composable
        fun Content() {
            val accounts: List<SelectAccountsModel.Page.Account> by model.accounts.collectAsState()
            ChipsFlowRow(
                all = accounts,
            ) { item ->
                var selected by item.selected.collectAsMutableAccessor()
                AccountContent(
                    info = item.info,
                    selected = selected,
                    onClick = { selected = !selected },
                )
            }
        }
    }

    @Composable
    fun Content() {
        val selectedAccounts: NonEmptyList<AccountInfo>? by model.selectedAccounts.collectAsState()
        val hasSelectedAccounts: Boolean = selectedAccounts != null
        Label(
            selected = model.isFocused.collectAsState().value,
            onClick = model.requestFocus,
            containerColor = hasSelectedAccounts.foldBoolean(
                ifTrue = { MaterialTheme.colorScheme.primaryContainer },
                ifFalse = { MaterialTheme.colorScheme.surfaceContainer },
            ),
        ) {
            Text(
                selectedAccounts.foldNullable(
                    ifNull = { stringResource(Res.string.accounts) },
                    ifNotNull = { accounts ->
                        remember(accounts) {
                            listOfNotNull(
                                accounts
                                    .take(maxCount)
                                    .joinToString(transform = AccountInfo::title),
                                accounts
                                    .drop(maxCount)
                                    .size
                                    .takeIf { it > 0 }
                                    ?.let { "+$it" }
                            ).joinToString(
                                separator = " ",
                            )
                        }
                    }
                )
            )
        }
    }

    companion object {

        private const val maxCount: Int = 1
    }
}