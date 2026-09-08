package org.hnau.pinfin.model.filter

import arrow.core.toNonEmptyListOrNull
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.foldRaw
import org.hnau.pinfin.data.records.FilteredRecords
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

fun filterOrNull(
    transaction: Transaction<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, *>,
    filters: Filters,
): Transaction<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, FilteredRecords<KeyValue<CategoryId, CategoryInfo>>>? {
    val accountSet = filters.accounts?.toSet()
    val period = filters.period
    return transaction.type.foldRaw(
        ifEntry = { variant ->
            val accountPasses = accountSet?.let { accounts -> variant.account.key in accounts } ?: true
            val periodPasses = period?.let { range -> transaction.timestamp in range } ?: true
            if (!accountPasses || !periodPasses) {
                null
            } else {
                val categorySet = filters.categories?.toSet()
                val records: List<Record<KeyValue<CategoryId, CategoryInfo>>> = variant.records.records.toList()
                val (matching, additional) = if (categorySet == null) {
                    records to emptyList()
                } else {
                    records.partition { record -> record.category.key in categorySet }
                }
                matching
                    .toNonEmptyListOrNull()
                    ?.let { main ->
                        Transaction(
                            timestamp = transaction.timestamp,
                            comment = transaction.comment,
                            type = Transaction.Type.Entry(
                                account = variant.account,
                                records = FilteredRecords(
                                    main = main,
                                    additional = additional,
                                ),
                            ),
                        )
                    }
            }
        },
        ifTransfer = { variant ->
            val accountPasses = accountSet?.let { accounts ->
                variant.from.key in accounts || variant.to.key in accounts
            } ?: true
            val periodPasses = period?.let { range -> transaction.timestamp in range } ?: true
            val categoryPasses = filters
                .categories
                ?.let { categories -> null in categories }
                ?: true
            if (accountPasses && periodPasses && categoryPasses) {
                Transaction(
                    timestamp = transaction.timestamp,
                    comment = transaction.comment,
                    type = variant,
                )
            } else {
                null
            }
        },
    )
}