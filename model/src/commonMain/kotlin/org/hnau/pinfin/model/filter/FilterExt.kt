package org.hnau.pinfin.model.filter

import arrow.core.NonEmptySet
import kotlinx.datetime.LocalDateRange
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo
import org.hnau.pinfin.model.utils.budget.state.fold

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
    return transaction.type.fold(
        ifEntry = { _, records ->
            records
                .any { record ->
                    record.idWithCategory.key in set
                }
        },
        ifTransfer = { _, _, _ -> null in set },
    )
}

private fun NonEmptySet<AccountId>?.checkAccounts(
    transaction: TransactionInfo,
): Boolean {
    if (this == null) {
        return true
    }
    val set = toSet()
    return transaction.type.fold(
        ifEntry = { idWithAccount, _ ->
            idWithAccount.key in set
        },
        ifTransfer = { from, to, _ ->
            from.key in set || to.key in set
        },
    )
}

private fun LocalDateRange?.checkPeriod(
    transaction: TransactionInfo,
): Boolean {
    if (this == null) {
        return true
    }
    val date = transaction.timestamp

    return date in this
}