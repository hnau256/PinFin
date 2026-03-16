@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import org.hnau.commons.app.model.ListScrollState
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.budgetstack.BudgetStackOpener
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.commons.gen.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class AccountsModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val budgetsRepository: BudgetRepository

        val budgetStackOpener: BudgetStackOpener
    }

    @Serializable
    data class Skeleton(
        val scrollState: MutableStateFlow<ListScrollState> =
            ListScrollState.Companion.initial.toMutableStateFlowAsInitial(),
    )

    val scrollState: MutableStateFlow<ListScrollState>
        get() = skeleton.scrollState

    val accounts: StateFlow<NonEmptyList<AccountInfo>?> = dependencies
        .budgetsRepository
        .state
        .mapState(scope) { budgetState ->
            budgetState
                .accounts
                .filter(AccountInfo::visible)
                .toNonEmptyListOrNull()
        }

    fun onAccountClick(
        account: AccountInfo,
    ) {
        dependencies
            .budgetStackOpener
            .openConfigAccount(
                info = account,
            )
    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}