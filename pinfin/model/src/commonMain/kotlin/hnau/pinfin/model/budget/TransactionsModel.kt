package hnau.pinfin.model.budget

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.coroutines.mapState
import hnau.pinfin.data.budget.BudgetRepository
import hnau.pinfin.data.budget.BudgetState
import hnau.pinfin.data.budget.TransactionInfo
import hnau.pinfin.model.budgetstack.BudgetStackOpener
import hnau.pinfin.model.budgetstack.BudgetStackOpenerImpl
import hnau.pinfin.data.dto.Transaction
import hnau.pinfin.data.dto.TransactionType
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class TransactionsModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val a: Int = 0,
    )

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
}