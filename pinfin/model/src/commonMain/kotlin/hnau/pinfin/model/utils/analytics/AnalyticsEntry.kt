package hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import hnau.pinfin.data.Amount
import hnau.pinfin.model.utils.amount
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.budget.state.TransactionInfo

data class AnalyticsEntry(
    val account: AccountInfo,
    val category: CategoryInfo?,
    val amount: Amount,
    val transaction: TransactionInfo,
)

fun TransactionInfo.toAnalyticsEntries(): NonEmptyList<AnalyticsEntry> = when (type) {
    is TransactionInfo.Type.Transfer -> nonEmptyListOf(
        AnalyticsEntry(
            account = type.from,
            category = null,
            amount = -amount,
            transaction = this,
        ),
        AnalyticsEntry(
            account = type.to,
            category = null,
            amount = amount,
            transaction = this,
        )
    )

    is TransactionInfo.Type.Entry -> type
        .records
        .map { record ->
            AnalyticsEntry(
                account = type.account,
                category = record.category,
                amount = amount,
                transaction = this,
            )
        }
}