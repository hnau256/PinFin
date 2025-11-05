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
            val values: NonEmptyList<Value>,
        ) {

            data class Value(
                val key: GroupKey?,
                val transactions: NonEmptyList<TransactionInfo>,
                val amount: Amount,
            )

            private val amounts: NonEmptyList<Amount> = values
                .map(Value::amount)

            val amountsRange: ClosedRange<Amount> = amounts.tail.fold(
                initial = amounts.head.let { first -> first..first },
            ) { acc, amount ->
                val min = acc.start
                val max = acc.endInclusive
                when {
                    amount < min -> amount..max
                    amount > max -> min..amount
                    else -> acc
                }
            }

            val sum: Amount = amounts.tail.fold(
                initial = amounts.head,
                operation = Amount::plus,
            )
        }

        suspend fun getContent(): Content?
    }

    suspend fun getItems(
        budgetState: BudgetState,
    ): NonEmptyList<Item>?
}