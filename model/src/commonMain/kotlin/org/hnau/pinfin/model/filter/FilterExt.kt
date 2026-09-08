package org.hnau.pinfin.model.filter

import arrow.core.toNonEmptyListOrNull
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.ifTrue
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
            val categorySet = filters.categories?.toSet()
            val records: List<Record<KeyValue<CategoryId, CategoryInfo>>> = variant.records.records.toList()
            val (matching, additional) = categorySet.foldNullable(
                ifNull = { records to emptyList() },
                ifNotNull = { categories ->
                    records.partition { record -> record.category.key in categories }
                },
            )
            val accountPasses = accountSet.foldNullable(
                ifNull = { true },
                ifNotNull = { accounts -> variant.account.key in accounts },
            )
            val periodPasses = period.foldNullable(
                ifNull = { true },
                ifNotNull = { range -> transaction.timestamp in range },
            )
            (accountPasses && periodPasses).ifTrue {
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
            val accountPasses = accountSet.foldNullable(
                ifNull = { true },
                ifNotNull = { accounts ->
                    variant.from.key in accounts || variant.to.key in accounts
                },
            )
            val periodPasses = period.foldNullable(
                ifNull = { true },
                ifNotNull = { range -> transaction.timestamp in range },
            )
            val categoryPasses = filters.categories.foldNullable(
                ifNull = { true },
                ifNotNull = { categories -> null in categories },
            )
            (accountPasses && periodPasses && categoryPasses).ifTrue {
                Transaction(
                    timestamp = transaction.timestamp,
                    comment = transaction.comment,
                    type = variant,
                )
            }
        },
    )
}