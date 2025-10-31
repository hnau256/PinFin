package hnau.pinfin.model.utils.budget.state

import hnau.common.kotlin.castOrNull
import hnau.pinfin.data.AccountConfig
import hnau.pinfin.data.AccountId
import hnau.pinfin.data.Amount
import hnau.pinfin.data.BudgetConfig
import hnau.pinfin.data.BudgetId
import hnau.pinfin.data.CategoryConfig
import hnau.pinfin.data.CategoryId
import hnau.pinfin.data.Record
import hnau.pinfin.data.Transaction
import hnau.pinfin.data.UpdateType
import hnau.pinfin.model.utils.amount
import hnau.pinfin.model.utils.budget.upchain.Upchain
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import hnau.pinfin.model.utils.budget.upchain.Update
import hnau.pinfin.model.utils.budget.upchain.plus
import hnau.pinfin.model.utils.budget.upchain.utils.getUpdatesAfterHashIfPossible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BudgetStateBuilder(
    private val hash: UpchainHash?,
    private val config: BudgetConfig,
    private val transactions: Map<Transaction.Id, Transaction>,
    private val accountsConfigs: Map<AccountId, AccountConfig>,
    private val categoriesConfigs: Map<CategoryId, CategoryConfig>,
) {

    override fun equals(
        other: Any?,
    ): Boolean = other
        ?.castOrNull<BudgetStateBuilder>()
        ?.takeIf { it.hash == hash } != null

    override fun hashCode(): Int = hash.hashCode()

    private operator fun plus(
        update: Update,
    ): BudgetStateBuilder {
        val updateType = UpdateType.updateTypeMapper.direct(update)
        val transactions = transactions.toMutableMap()
        val accountsConfigs = this@BudgetStateBuilder.accountsConfigs.toMutableMap()
        val categoriesConfigs = this@BudgetStateBuilder.categoriesConfigs.toMutableMap()
        var info = config
        when (updateType) {
            is UpdateType.RemoveTransaction ->
                transactions -= updateType.id

            is UpdateType.Transaction ->
                transactions += (updateType.id to updateType.transaction)

            is UpdateType.Config ->
                info += updateType.config

            is UpdateType.AccountConfig -> accountsConfigs[updateType.id] =
                accountsConfigs.getOrElse(updateType.id) { AccountConfig.empty } + updateType.config

            is UpdateType.CategoryConfig -> categoriesConfigs[updateType.id] =
                categoriesConfigs.getOrElse(updateType.id) { CategoryConfig.empty } + updateType.config
        }
        return BudgetStateBuilder(
            hash = hash + update,
            transactions = transactions,
            accountsConfigs = accountsConfigs,
            categoriesConfigs = categoriesConfigs,
            config = info,
        )
    }

    suspend fun withNewUpchain(
        newUpchain: Upchain,
    ): BudgetStateBuilder = withContext(Dispatchers.Default) {

        val additionalUpdates = newUpchain.getUpdatesAfterHashIfPossible(
            hash = hash,
        )
        val (state, updates) = when (additionalUpdates) {
            null -> empty to newUpchain.items.map(Upchain.Item::update)
            else -> this@BudgetStateBuilder to additionalUpdates
        }
        updates.fold(
            initial = state,
            operation = BudgetStateBuilder::plus,
        )
    }

    suspend fun toBudgetState(
        id: BudgetId,
    ): BudgetState = withContext(Dispatchers.Default) {

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
                        amount = Amount.Companion.zero,
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
                        amountOffset = type.amount,
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
                        amountOffset = -type.amount
                    )
                    useAccount(
                        id = type.to,
                        amountOffset = type.amount
                    )
                }
            }
        }

        BudgetState(
            hash = hash,
            transactions = transactions
                .map { (id, transaction) ->
                    TransactionInfo.Companion.fromTransaction(
                        id = id,
                        transaction = transaction,
                        categories = categories,
                        accounts = accounts,
                    )
                }
                .sortedByDescending(TransactionInfo::timestamp),
            categories = categories.values.toList(),
            accounts = accounts.values.toList(),
            info = BudgetInfo.create(
                id = id,
                config = config,
            )
        )
    }

    companion object {

        val empty = BudgetStateBuilder(
            hash = null,
            config = BudgetConfig.empty,
            accountsConfigs = emptyMap(),
            transactions = emptyMap(),
            categoriesConfigs = emptyMap(),
        )
    }
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