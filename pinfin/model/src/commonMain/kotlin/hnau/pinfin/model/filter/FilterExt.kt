package hnau.pinfin.model.filter

import arrow.core.NonEmptySet
import hnau.pinfin.data.AccountId
import hnau.pinfin.data.CategoryId
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun Filters.check(
    transaction: TransactionInfo,
): Boolean = when {
    !categories.checkCategories(transaction) -> false
    !accounts.checkAccounts(transaction) -> false
    !period.checkPeriod(transaction) -> false
    else -> true
}

private fun NonEmptySet<CategoryId?>?.checkCategories(
    transaction: TransactionInfo,
): Boolean {
    if (this == null) {
        return true
    }
    val set = toSet()
    return when (val type = transaction.type) {
        is TransactionInfo.Type.Entry -> type
            .records
            .any { record ->
                record.category.id in set
            }

        is TransactionInfo.Type.Transfer -> null in set
    }
}

private fun NonEmptySet<AccountId>?.checkAccounts(
    transaction: TransactionInfo,
): Boolean {
    if (this == null) {
        return true
    }
    val set = toSet()
    return when (val type = transaction.type) {
        is TransactionInfo.Type.Entry ->
            type.account.id in set

        is TransactionInfo.Type.Transfer ->
            type.from.id in set || type.to.id in set
    }
}

private fun LocalDateRange?.checkPeriod(
    transaction: TransactionInfo,
): Boolean {
    if (this == null) {
        return true
    }
    val date = transaction
        .timestamp
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    return date in this
}