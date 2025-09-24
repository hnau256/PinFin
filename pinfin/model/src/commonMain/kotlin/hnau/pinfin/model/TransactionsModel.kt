package hnau.pinfin.model

import hnau.common.app.model.ListScrollState
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.Delayed
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapStateDelayed
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.filter
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.budgetstack.BudgetStackOpener
import hnau.pinfin.model.filter.FilterModel
import hnau.pinfin.model.filter.Filters
import hnau.pinfin.model.filter.check
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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
        var scrollState: Pair<Filters, MutableStateFlow<ListScrollState>>? = null,
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

    val scrollState: StateFlow<ListScrollState> = filter
        .filters
        .flatMapState(scope) { filters ->
            skeleton::scrollState
                .toAccessor()
                .filter { it.first == filter }
                .getOrInit { filters to ListScrollState.initial.toMutableStateFlowAsInitial() }
                .second
        }

    fun updateScrollState(
        scrollState: ListScrollState,
    ) {
        skeleton.scrollState?.second?.value = scrollState
    }

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
        a = dependencies.budgetRepository.transactions.list,
        b = filter.filters,
        combine = ::Pair,
    ).mapStateDelayed(scope) { (transactions, filters) ->
        withContext(Dispatchers.Default) {
            transactions.filter { transaction ->
                filters.check(transaction)
            }
        }
    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}