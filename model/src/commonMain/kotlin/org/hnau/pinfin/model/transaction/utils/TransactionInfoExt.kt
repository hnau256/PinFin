package org.hnau.pinfin.model.transaction.utils

import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.foldRaw
import org.hnau.pinfin.data.records.SimpleRecords
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

fun <A, C, A1, C1> Transaction<A, C, *>.map(
    mapA: (A) -> A1,
    mapC: (C) -> C1,
): Transaction<A1, C1, *> = Transaction(
    timestamp = timestamp,
    comment = comment,
    type = type.foldRaw(
        ifEntry = { variant ->
            Transaction.Type.Entry(
                account = mapA(variant.account),
                records = SimpleRecords(
                    records = variant.records.records.map { record ->
                        Record(
                            category = mapC(record.category),
                            amount = record.amount,
                            comment = record.comment,
                        )
                    },
                ),
            )
        },
        ifTransfer = { variant ->
            Transaction.Type.Transfer(
                from = mapA(variant.from),
                to = mapA(variant.to),
                amount = variant.amount,
            )
        },
    ),
)

fun <C, C1> Record<C>.mapCategory(
    mapC: (C) -> C1,
): Record<C1> = Record(
    category = mapC(category),
    amount = amount,
    comment = comment,
)

fun Transaction<AccountId, CategoryId, *>.toResolved(
    categories: Map<CategoryId, CategoryInfo>,
    accounts: Map<AccountId, AccountInfo>,
): Transaction<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, *> = map(
    mapA = { id -> KeyValue(id, accounts.getValue(id)) },
    mapC = { id -> KeyValue(id, categories.getValue(id)) },
)

fun Transaction.Type<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, *>.toRawType(): Transaction.Type<AccountId, CategoryId, *> = foldRaw(
    ifEntry = { variant ->
        Transaction.Type.Entry(
            account = variant.account.key,
            records = SimpleRecords(
                records = variant.records.records.map { record ->
                    Record(
                        category = record.category.key,
                        amount = record.amount,
                        comment = record.comment,
                    )
                },
            ),
        )
    },
    ifTransfer = { variant ->
        Transaction.Type.Transfer(
            from = variant.from.key,
            to = variant.to.key,
            amount = variant.amount,
        )
    },
)