package hnau.pinfin.client.model.budget

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapState
import hnau.pinfin.client.data.budget.BudgetRepository
import hnau.pinfin.client.model.budgetstack.EditTransactionOpener
import hnau.pinfin.client.model.budgetstack.NewTransactionOpener
import hnau.pinfin.scheme.Transaction
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

        val newTransactionsOpener: NewTransactionOpener

        val editTransactionOpener: EditTransactionOpener
    }

    val onAddTransactionClick: () -> Unit
        get() = dependencies.newTransactionsOpener::openNewTransaction

    val onEditTransactionClick: (id: Transaction.Id) -> Unit
        get() = dependencies.editTransactionOpener::openEditTransaction

    val transactions: StateFlow<Loadable<NonEmptyList<Pair<Transaction.Id, Transaction>>?>>
        get() = dependencies
            .budgetRepository
            .transaction
            .list
            .mapState(
                scope = scope,
            ) { transactionsOrLoading ->
                transactionsOrLoading.map { transactions ->
                    transactions
                        .asReversed()
                        .toNonEmptyListOrNull()
                }
            }
}