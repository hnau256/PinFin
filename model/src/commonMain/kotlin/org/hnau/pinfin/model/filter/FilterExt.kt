package org.hnau.pinfin.model.filter

import arrow.core.toNonEmptyListOrNull
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.ifFalse
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.foldRaw
import org.hnau.pinfin.data.records.FilteredRecord
import org.hnau.pinfin.model.utils.budget.state.AccountIdWithInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryIdWithInfo

internal fun Transaction<AccountIdWithInfo, CategoryIdWithInfo, *>.applyFiltersOrNull(
    filters: Filters,
): Transaction<AccountIdWithInfo, CategoryIdWithInfo, FilteredRecord<CategoryIdWithInfo>>? {
    val accountSet = filters.accounts?.toSet()
    val period = filters.period
    period
        .foldNullable(
            ifNull = { true },
            ifNotNull = { range -> timestamp in range },
        )
        .ifFalse { return null }

    val type = type
        .foldRaw(
            ifEntry = { variant ->

                val categorySet = filters.categories?.toSet()

                val records: List<Record<CategoryIdWithInfo>> =
                    variant.records.map { it.record }

                val (matching, additional) =
                    categorySet.foldNullable(
                        ifNull = { records to emptyList() },
                        ifNotNull = { categories ->
                            records.partition { record -> record.category.key in categories }
                        },
                    )

                val main = matching
                    .toNonEmptyListOrNull()
                    ?: return@foldRaw null

                accountSet
                    .foldNullable(
                        ifNull = { true },
                        ifNotNull = { accounts -> variant.account.key in accounts },
                    )
                    .ifFalse { return@foldRaw null }

                Transaction.Type.Entry(
                    account = variant.account,
                    records = main.map { record -> FilteredRecord(record = record, included = true) } +
                        additional.map { record -> FilteredRecord(record = record, included = false) },
                )
            },
            ifTransfer = { variant ->

                accountSet
                    .foldNullable(
                        ifNull = { true },
                        ifNotNull = { accounts ->
                            variant.from.key in accounts || variant.to.key in accounts
                        },
                    )
                    .ifFalse { return@foldRaw null }

                filters
                    .categories
                    .foldNullable(
                        ifNull = { true },
                        ifNotNull = { categories -> null in categories },
                    )
                    .ifFalse { return@foldRaw null }

                variant
            },
        )
        ?: return null

    return Transaction(
        timestamp = timestamp,
        comment = comment,
        type = type,
    )
}