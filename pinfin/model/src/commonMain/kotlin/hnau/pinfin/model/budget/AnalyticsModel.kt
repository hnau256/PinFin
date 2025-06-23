@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budget

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import hnau.common.kotlin.coroutines.mapState
import hnau.common.model.ListScrollState
import hnau.common.model.goback.GoBackHandler
import hnau.common.model.goback.GoBackHandlerProvider
import hnau.common.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.budgetstack.BudgetStackOpener
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class AnalyticsModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        val budgetsRepository: BudgetRepository

        val budgetStackOpener: BudgetStackOpener
    }

    @Serializable
    data class Skeleton(
        val scrollState: MutableStateFlow<ListScrollState> =
            ListScrollState.initial.toMutableStateFlowAsInitial(),
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

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}