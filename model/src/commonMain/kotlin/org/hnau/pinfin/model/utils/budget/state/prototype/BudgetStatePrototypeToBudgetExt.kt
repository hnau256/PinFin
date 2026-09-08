package org.hnau.pinfin.model.utils.budget.state.prototype

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.foldRaw
import org.hnau.pinfin.data.plus
import org.hnau.pinfin.model.transaction.utils.toResolved
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.BudgetInfo
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.pinfin.model.utils.totalAmount

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
                    amountOffset = variant.records.totalAmount(
                        currency = info.currency,
                    ),
                )
                variant.records.records.forEach { record ->
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
                val resolved = transaction.toResolved(
                    categories = categories,
                    accounts = accounts,
                )
                KeyValue(id, resolved)
            }
            .sortedBy { it.value.timestamp },
        categories = categories.map { (key, value) -> KeyValue(key, value) },
        accounts = accounts.map { (key, value) -> KeyValue(key, value) },
        info = info,
    )
}