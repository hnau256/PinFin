package hnau.pinfin.model.filter

import arrow.core.NonEmptyList
import hnau.pinfin.data.CategoryId
import hnau.pinfin.model.utils.budget.state.TransactionInfo

internal fun Filters.check(
    transaction: TransactionInfo,
): Boolean = when {
    !selectedCategories.check(transaction) -> false
    else -> true
}

private fun NonEmptyList<CategoryId>?.check(
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

        is TransactionInfo.Type.Transfer -> false
    }
}