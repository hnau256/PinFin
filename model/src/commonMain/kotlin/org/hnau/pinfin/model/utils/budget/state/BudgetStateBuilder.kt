package org.hnau.pinfin.model.utils.budget.state

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.castOrNull
import org.hnau.pinfin.data.AccountConfig
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.BudgetConfig
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.CategoryConfig
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.model.utils.amount
import org.hnau.upchain.core.Upchain
import org.hnau.upchain.core.UpchainHash
import org.hnau.upchain.core.Update
import org.hnau.upchain.core.calcNext
import org.hnau.upchain.core.getUpdatesAfterHashIfPossible

data class BudgetStateBuilder(
    private val hash: UpchainHash?,
    private val config: BudgetConfig,
    private val dependencies: Dependencies,
    private val transactions: Map<Transaction.Id, Transaction>,
    private val accountsConfigs: Map<AccountId, AccountConfig>,
    private val categoriesConfigs: Map<CategoryId, CategoryConfig>,
) {

    @Pipe
    interface Dependencies {

        val currency: Currency
    }

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
            hash = hash.calcNext(
                update = update,
            ),
            transactions = transactions,
            accountsConfigs = accountsConfigs,
            categoriesConfigs = categoriesConfigs,
            config = info,
            dependencies = dependencies,
        )
    }

    suspend fun withNewUpchain(
        newUpchain: Upchain,
    ): BudgetStateBuilder = withContext(Dispatchers.Default) {

        val additionalUpdates = newUpchain.getUpdatesAfterHashIfPossible(
            hash = hash,
        )
        val (state, updates) = when (additionalUpdates) {
            null -> empty(
                dependencies = dependencies,
            ) to newUpchain.items.map(Upchain.Item::update)

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
                            currency = dependencies.currency,
                        ),
                    )
                    type.records.forEach { record ->
                        useCategory(
                            id = record.category,
                        )
                    }
                }

                is Transaction.Type.Transfer -> {
                    val amount = type.amount.toAmount(dependencies.currency.scale)
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
                    TransactionInfo.fromTransaction(
                        id = id,
                        transaction = transaction,
                        categories = categories,
                        accounts = accounts,
                    )
                }
                .sortedBy(TransactionInfo::timestamp),
            categories = categories.values.toList(),
            accounts = accounts.values.toList(),
            info = BudgetInfo.create(
                id = id,
                config = config,
            )
        )
    }

    companion object {

        fun empty(
            dependencies: Dependencies,
        ): BudgetStateBuilder = BudgetStateBuilder(
            hash = null,
            config = BudgetConfig.empty,
            accountsConfigs = emptyMap(),
            transactions = emptyMap(),
            categoriesConfigs = emptyMap(),
            dependencies = dependencies
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