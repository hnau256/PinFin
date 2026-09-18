package org.hnau.pinfin.model.utils.budget.state

import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.model.utils.budget.state.prototype.BudgetStatePrototype
import org.hnau.upchain.core.UpchainHash

data class BudgetState(
    val prototype: BudgetStatePrototype,
    val info: BudgetInfo,
    val transactions: List<KeyValue<Transaction.Id, Transaction<AccountIdWithInfo, CategoryIdWithInfo, *>>>,
    val categories: List<CategoryIdWithInfo>,
    val accounts: List<AccountIdWithInfo>,
) {
    val hash: UpchainHash?
        get() = prototype.hash

    val visibleAccounts: List<AccountIdWithInfo> by lazy {
        accounts.filter { idWithAccount ->
            idWithAccount.value.visible
        }
    }
}