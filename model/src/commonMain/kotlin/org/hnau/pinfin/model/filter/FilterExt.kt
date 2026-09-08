package org.hnau.pinfin.model.filter

import arrow.core.NonEmptySet
import kotlinx.datetime.LocalDateRange
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.fold
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

internal fun Filters.check(
    transaction: Transaction<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, *>,
): Boolean = when {
    !categories.checkCategories(transaction) -> false
    !accounts.checkAccounts(transaction) -> false
    !period.checkPeriod(transaction) -> false
    else -> true
}

private fun NonEmptySet<CategoryId?>?.checkCategories(
    transaction: Transaction<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, *>,
): Boolean {
    if (this == null) {
        return true
    }
    val set = toSet()
    return transaction.type.fold(
        ifEntry = { _, records ->
            records
                .records
                .any { record ->
                    record.category.key in set
                }
        },
        ifTransfer = { _, _, _ -> null in set },
    )
}

private fun NonEmptySet<AccountId>?.checkAccounts(
    transaction: Transaction<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, *>,
): Boolean {
    if (this == null) {
        return true
    }
    val set = toSet()
    return transaction.type.fold(
        ifEntry = { account, _ ->
            account.key in set
        },
        ifTransfer = { from, to, _ ->
            from.key in set || to.key in set
        },
    )
}

private fun LocalDateRange?.checkPeriod(
    transaction: Transaction<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, *>,
): Boolean {
    if (this == null) {
        return true
    }
    val date = transaction.timestamp

    return date in this
}