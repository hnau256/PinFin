@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.client.model

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.goback.GlobalGoBackHandler
import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.client.data.budget.BudgetRepository
import hnau.pinfin.scheme.Transaction
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TransactionsModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    val onAddTransactionClick: () -> Unit,
    val onEditTransactionClick: (id: Transaction.Id, transition: Transaction) -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val a: Int = 0,
    )

    @Shuffle
    interface Dependencies {

        val budgetRepository: BudgetRepository

        val globalGoBackHandler: GlobalGoBackHandler
    }

    val globalGoBackHandler: GoBackHandler = dependencies
        .globalGoBackHandler
        .resolve(scope)

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