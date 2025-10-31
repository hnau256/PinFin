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
            val getValues: suspend () -> Map<GroupKey?, Value>,
        ) {

            data class Value(
                val transactions: NonEmptyList<TransactionInfo>,
                val amount: Amount,
            )
        }

        val content: Content?
    }

    suspend fun getItems(
        budgetState: BudgetState,
    ): NonEmptyList<Item>?
}