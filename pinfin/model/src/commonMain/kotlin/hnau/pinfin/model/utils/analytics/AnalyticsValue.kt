package hnau.pinfin.model.utils.analytics

import hnau.pinfin.data.AccountId
import hnau.pinfin.data.Amount
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.data.CategoryId
import hnau.pinfin.model.utils.budget.state.TransactionInfo

data class AnalyticsValue(
    val value: Amount,
    val tag: Tag,
    val usedTransactions: List<TransactionInfo>,
) {

    sealed interface Tag {

        data class Direction(
            val direction: AmountDirection,
        ) : Tag

        data class Category(
            val category: CategoryId,
        ) : Tag

        data class Account(
            val account: AccountId,
        ) : Tag
    }
}