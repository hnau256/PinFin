package hnau.pinfin.client.data.budget

import hnau.common.kotlin.castOrNull
import hnau.pinfin.client.data.utils.SignedAmount
import hnau.pinfin.client.data.utils.signedAmount
import hnau.pinfin.scheme.AccountId
import hnau.pinfin.scheme.CategoryId
import hnau.pinfin.scheme.Record
import hnau.pinfin.scheme.Transaction
import hnau.pinfin.scheme.Update

class BudgetStateBuilder {

    private var updatesCount = 0

    private val transactions: MutableMap<Transaction.Id, Transaction> = mutableMapOf()


    fun applyUpdate(
        update: Update,
    ) {
        updatesCount++
        when (update) {
            is Update.RemoveTransaction -> transactions.remove(
                key = update.id,
            )

            is Update.Transaction -> transactions.set(
                key = update.id,
                value = update.transaction,
            )
        }
    }

    fun toBudgetState(): BudgetState {

        val categories: MutableMap<CategoryId, CategoryInfo> = mutableMapOf()
        val accounts: MutableMap<AccountId, AccountInfo> = mutableMapOf()

        fun useCategory(
            id: CategoryId,
        ) {
            categories[id] = CategoryInfo(
                id = id,
            )
        }

        fun useAccount(
            id: AccountId,
            amountOffset: SignedAmount,
        ) {
            accounts[id] = accounts
                .getOrElse(id) {
                    AccountInfo(
                        id = id,
                        amount = SignedAmount.zero,
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
                        amountOffset = type.signedAmount,
                    )
                    type.records.forEach { record ->
                        useCategory(
                            id = record.category,
                        )
                    }
                }

                is Transaction.Type.Transfer -> {
                    useAccount(
                        id = type.from,
                        amountOffset = SignedAmount(
                            amount = type.amount,
                            positive = false,
                        )
                    )
                    useAccount(
                        id = type.to,
                        amountOffset = SignedAmount(
                            amount = type.amount,
                            positive = true,
                        )
                    )
                }
            }
        }

        return BudgetState(
            transactions = transactions
                .map { (id, transaction) ->
                    TransactionInfo.fromTransaction(
                        id = id,
                        transaction = transaction,
                        categories = categories,
                        accounts = accounts,
                    )
                }
                .sortedByDescending(TransactionInfo::timestamp),
            categories = categories.values.toList(),
            accounts = accounts.values.toList()
        )
    }

    override fun equals(other: Any?): Boolean = other
        .castOrNull<BudgetStateBuilder>()
        ?.takeIf { it.updatesCount == updatesCount } != null

    override fun hashCode(): Int = updatesCount
}

private fun TransactionInfo.Companion.fromTransaction(
    id: Transaction.Id,
    transaction: Transaction,
    categories: Map<CategoryId, CategoryInfo>,
    accounts: Map<AccountId, AccountInfo>,
): TransactionInfo = TransactionInfo(
    id = id,
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