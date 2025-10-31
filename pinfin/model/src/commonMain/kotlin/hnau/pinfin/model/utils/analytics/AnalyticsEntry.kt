package hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import hnau.pinfin.data.AccountId
import hnau.pinfin.data.Amount
import hnau.pinfin.data.CategoryId
import hnau.pinfin.model.utils.amount
import hnau.pinfin.model.utils.budget.state.TransactionInfo

data class AnalyticsEntry(
    val account: AccountId,
    val category: CategoryId?,
    val amount: Amount,
)

fun TransactionInfo.toAnalyticsEntries(): NonEmptyList<AnalyticsEntry> = when (type) {
    is TransactionInfo.Type.Transfer -> nonEmptyListOf(
        AnalyticsEntry(
            account = type.from.id,
            category = null,
            amount = -amount,
        ),
        AnalyticsEntry(
            account = type.to.id,
            category = null,
            amount = amount,
        )
    )

    is TransactionInfo.Type.Entry -> type
        .records
        .map { record ->
            AnalyticsEntry(
                account = type.account.id,
                category = record.category.id,
                amount = amount,
            )
        }
}