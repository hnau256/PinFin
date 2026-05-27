package org.hnau.pinfin.model.utils.budget.state

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.model.utils.amount

suspend fun BudgetStatePrototype.toBudgetState(
    id: BudgetId,
): BudgetState = withContext(Dispatchers.Default) {

    val info = BudgetInfo.create(
        id = id,
        config = config,
    )

    val categories: MutableMap<CategoryId, CategoryInfo> = mutableMapOf()
    val accounts: MutableMap<AccountId, AccountInfo> = mutableMapOf()

    fun useCategory(
        id: CategoryId,
    ) {
        categories[id] = CategoryInfo(
            id = id,
            config = categoriesConfigs[id],
        )
    }

    fun useAccount(
        id: AccountId,
        amountOffset: Amount,
    ) {
        accounts[id] = accounts
            .getOrElse(id) {
                AccountInfo(
                    id = id,
                    amount = Amount.zero,
                    config = accountsConfigs[id],
                )
            }
            .let { currentInfo ->
                currentInfo.copy(
                    amount = currentInfo.amount + amountOffset
                )
            }
    }

    transactions.forEach { (_, transaction) ->
        when (val type = transaction.type) {
            is Transaction.Type.Entry -> {
                useAccount(
                    id = type.account,
                    amountOffset = type.amount(
                        currency = info.currency,
                    ),
                )
                type.records.forEach { record ->
                    useCategory(
                        id = record.category,
                    )
                }
            }

            is Transaction.Type.Transfer -> {
                val amount = type.amount.toAmount(info.currency.scale)
                useAccount(
                    id = type.from,
                    amountOffset = -amount
                )
                useAccount(
                    id = type.to,
                    amountOffset = amount
                )
            }
        }
    }

    BudgetState(
        hash = hash,
        transactions = transactions
            .map { (id, transaction) ->
                val transaction = TransactionInfo.fromTransaction(
                    transaction = transaction,
                    categories = categories,
                    accounts = accounts,
                )
                KeyValue(id, transaction)
            }
            .sortedBy { it.value.timestamp },
        categories = categories.values.toList(),
        accounts = accounts.values.toList(),
        info = info,
    )
}

private fun TransactionInfo.Companion.fromTransaction(
    transaction: Transaction,
    categories: Map<CategoryId, CategoryInfo>,
    accounts: Map<AccountId, AccountInfo>,
): TransactionInfo = TransactionInfo(
    timestamp = transaction.timestamp,
    comment = transaction.comment,
    type = TransactionInfo.Type.fromType(
        type = transaction.type,
        categories = categories,
        accounts = accounts,
    )
)

private fun TransactionInfo.Type.Companion.fromType(
    type: Transaction.Type,
    categories: Map<CategoryId, CategoryInfo>,
    accounts: Map<AccountId, AccountInfo>,
): TransactionInfo.Type = when (type) {
    is Transaction.Type.Entry -> TransactionInfo.Type.Entry.fromEntry(
        entry = type,
        categories = categories,
        accounts = accounts,
    )

    is Transaction.Type.Transfer -> TransactionInfo.Type.Transfer.fromTransfer(
        transfer = type,
        accounts = accounts,
    )
}

private fun TransactionInfo.Type.Entry.Companion.fromEntry(
    entry: Transaction.Type.Entry,
    categories: Map<CategoryId, CategoryInfo>,
    accounts: Map<AccountId, AccountInfo>,
): TransactionInfo.Type.Entry = TransactionInfo.Type.Entry(
    account = accounts.getValue(entry.account),
    records = entry
        .records
        .map { record ->
            TransactionInfo.Type.Entry.Record.fromRecord(
                record = record,
                categories = categories,
            )
        }
)

private fun TransactionInfo.Type.Entry.Record.Companion.fromRecord(
    record: Record,
    categories: Map<CategoryId, CategoryInfo>,
): TransactionInfo.Type.Entry.Record = TransactionInfo.Type.Entry.Record(
    category = categories.getValue(record.category),
    amount = record.amount,
    comment = record.comment,
)

private fun TransactionInfo.Type.Transfer.Companion.fromTransfer(
    transfer: Transaction.Type.Transfer,
    accounts: Map<AccountId, AccountInfo>,
): TransactionInfo.Type.Transfer = TransactionInfo.Type.Transfer(
    from = accounts.getValue(transfer.from),
    to = accounts.getValue(transfer.to),
    amount = transfer.amount,
)