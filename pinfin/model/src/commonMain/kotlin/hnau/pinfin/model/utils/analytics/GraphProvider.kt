package hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptyList
import hnau.pinfin.data.Amount
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import kotlinx.datetime.LocalDateRange

interface GraphProvider {

    interface Item {

        val period: LocalDateRange

        data class Content(
            val getTransactions: suspend () -> NonEmptyList<TransactionInfo>,
            val getValues: suspend () -> Map<GroupKey?, Amount>,
        )

        val content: Content?
    }

    suspend fun getItems(
        budgetState: BudgetState,
    ): NonEmptyList<Item>?
}