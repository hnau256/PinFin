package org.hnau.pinfin.model

import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.Delayed
import org.hnau.commons.kotlin.coroutines.flow.state.combineState
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.mapStateDelayed
import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.model.budgetstack.BudgetStackOpener
import org.hnau.pinfin.model.filter.FilterModel
import org.hnau.pinfin.model.filter.Filters
import org.hnau.pinfin.model.filter.check
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo
import org.hnau.commons.gen.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

class TransactionsModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        val budgetStackOpener: BudgetStackOpener

        fun filter(): FilterModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var filter: FilterModel.Skeleton,
    ) {

        companion object {

            fun create(
                initialFilters: Filters = Filters.empty,
            ): Skeleton = Skeleton(
                filter = FilterModel.Skeleton.create(
                    initialFilters = initialFilters,
                )
            )
        }
    }

    val filter = FilterModel(
        scope = scope,
        dependencies = dependencies.filter(),
        skeleton = skeleton.filter,
    )

    fun onAddTransactionClick() {
        dependencies
            .budgetStackOpener
            .openNewTransaction(
                transactionType = TransactionType.Companion.default,
            )
    }

    val onEditTransactionClick: (TransactionInfo) -> Unit
        get() = dependencies.budgetStackOpener::openEditTransaction

    val transactions: StateFlow<Loadable<Delayed<List<TransactionInfo>>>> = combineState(
        scope = scope,
        first = dependencies
            .budgetRepository
            .state
            .mapState(scope) { it.transactions.asReversed() },
        second = filter.filters,
        combine = ::Pair,
    ).mapStateDelayed(scope) { (transactions, filters) ->
        withContext(Dispatchers.Default) {
            transactions.filter { transaction ->
                filters.check(transaction)
            }
        }
    }

    val goBackHandler: GoBackHandler
        get() = filter.goBackHandler
}