package org.hnau.pinfin.model.utils.budget.state

import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.model.utils.budget.state.prototype.BudgetStatePrototype
import org.hnau.upchain.core.UpchainHash

data class BudgetState(
    val prototype: BudgetStatePrototype,
    val info: BudgetInfo,
    val transactions: List<KeyValue<Transaction.Id, TransactionInfo>>,
    val categories: List<KeyValue<CategoryId, CategoryInfo>>,
    val accounts: List<KeyValue<AccountId, AccountInfo>>,
) {
    val hash: UpchainHash?
        get() = prototype.hash

    val visibleAccounts: List<KeyValue<AccountId, AccountInfo>> by lazy {
        accounts.filter { idWithAccount ->
            idWithAccount.value.visible
        }
    }
}