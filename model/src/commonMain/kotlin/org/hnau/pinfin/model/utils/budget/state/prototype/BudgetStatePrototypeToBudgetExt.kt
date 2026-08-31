package org.hnau.pinfin.model.utils.budget.state.prototype

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.foldRaw
import org.hnau.pinfin.data.plus
import org.hnau.pinfin.model.utils.amount
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.BudgetInfo
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo

suspend fun BudgetStatePrototype.toBudgetState(
    id: BudgetId,
): BudgetState = withContext(Dispatchers.Default) {

    val info = BudgetInfo.Companion.create(
        id = id,
        config = config,
    )

    val categories: MutableMap<CategoryId, CategoryInfo> = mutableMapOf()
    val accounts: MutableMap<AccountId, AccountInfo> = mutableMapOf()

    fun useCategory(
        id: CategoryId,
    ) {
        categories[id] = CategoryInfo.create(
            id = id,
            config = categoriesConfigs[id],
        )
    }

    fun useAccount(
        id: AccountId,
        amountOffset: KeyValue<AmountDirection, Amount>,
    ) {
        accounts[id] = accounts
            .getOrElse(id) {
                AccountInfo.create(
                    id = id,
                    amount = KeyValue(AmountDirection.Debit, Amount.zero),
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
        transaction.type.foldRaw(
            ifEntry = { variant ->
                useAccount(
                    id = variant.account,
                    amountOffset = variant.amount(
                        currency = info.currency,
                    ),
                )
                variant.records.forEach { record ->
                    useCategory(
                        id = record.category,
                    )
                }
            },
            ifTransfer = { variant ->
                val amount = variant.amount.toAmount(info.currency.scale)
                useAccount(
                    id = variant.from,
                    amountOffset = KeyValue(AmountDirection.Debit, amount)
                )
                useAccount(
                    id = variant.to,
                    amountOffset = KeyValue(AmountDirection.Credit, amount)
                )
            },
        )
    }

    BudgetState(
        prototype = this@toBudgetState,
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
        categories = categories.map { (key, value) -> KeyValue(key, value) },
        accounts = accounts.map { (key, value) -> KeyValue(key, value) },
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
): TransactionInfo.Type = type.foldRaw(
    ifEntry = { variant ->
        TransactionInfo.Type.Entry.fromEntry(
            entry = variant,
            categories = categories,
            accounts = accounts,
        )
    },
    ifTransfer = { variant ->
        TransactionInfo.Type.Transfer.fromTransfer(
            transfer = variant,
            accounts = accounts,
        )
    },
)

private fun TransactionInfo.Type.Entry.Companion.fromEntry(
    entry: Transaction.Type.Entry,
    categories: Map<CategoryId, CategoryInfo>,
    accounts: Map<AccountId, AccountInfo>,
): TransactionInfo.Type.Entry = TransactionInfo.Type.Entry(
    idWithAccount = KeyValue(
        key = entry.account,
        value = accounts.getValue(entry.account),
    ),
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
    idWithCategory = KeyValue(
        key = record.category,
        value = categories.getValue(record.category),
    ),
    amount = record.amount,
    comment = record.comment,
)

private fun TransactionInfo.Type.Transfer.Companion.fromTransfer(
    transfer: Transaction.Type.Transfer,
    accounts: Map<AccountId, AccountInfo>,
): TransactionInfo.Type.Transfer = TransactionInfo.Type.Transfer(
    from = KeyValue(
        key = transfer.from,
        value = accounts.getValue(transfer.from),
    ),
    to = KeyValue(
        key = transfer.to,
        value = accounts.getValue(transfer.to),
    ),
    amount = transfer.amount,
)