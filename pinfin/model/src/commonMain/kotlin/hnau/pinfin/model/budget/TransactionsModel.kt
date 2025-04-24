@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budget

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.ListScrollState
import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.dto.TransactionType
import hnau.pinfin.data.repository.BudgetRepository
import hnau.pinfin.data.repository.TransactionInfo
import hnau.pinfin.model.budgetstack.BudgetStackOpener
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TransactionsModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val scrollState: MutableStateFlow<ListScrollState> =
            ListScrollState.initial.toMutableStateFlowAsInitial(),
    )

    val scrollState: MutableStateFlow<ListScrollState>
        get() = skeleton.scrollState

    @Shuffle
    interface Dependencies {

        val budgetRepository: BudgetRepository

        val budgetStackOpener: BudgetStackOpener
    }

    fun onAddTransactionClick() {
        dependencies
            .budgetStackOpener
            .openNewTransaction(
                transactionType = TransactionType.default,
            )
    }

    val onEditTransactionClick: (TransactionInfo) -> Unit
        get() = dependencies.budgetStackOpener::openEditTransaction

    val transactions: StateFlow<NonEmptyList<TransactionInfo>?>
        get() = dependencies
            .budgetRepository
            .transactions
            .list
            .mapState(
                scope = scope,
            ) { transactions ->
                transactions.toNonEmptyListOrNull()
            }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}