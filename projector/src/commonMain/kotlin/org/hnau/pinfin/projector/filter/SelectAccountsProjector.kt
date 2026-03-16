package org.hnau.pinfin.projector.filter

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import arrow.core.NonEmptyList
import org.hnau.commons.app.projector.uikit.row.ChipsFlowRow
import org.hnau.commons.app.projector.utils.collectAsMutableAccessor
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.model.filter.pageable.SelectAccountsModel
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.AccountContent
import org.hnau.pinfin.projector.utils.Label

class SelectAccountsProjector(
    private val model: SelectAccountsModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    class Page(
        private val model: SelectAccountsModel.Page,
        private val dependencies: Dependencies,
    ) {

        @Pipe
        interface Dependencies {

            val localization: Localization
        }

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
                    localization = dependencies.localization,
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
                    ifNull = { (dependencies.localization.accounts) },
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