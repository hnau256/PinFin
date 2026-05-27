package org.hnau.pinfin.model.utils.budget.state

import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.model.utils.budget.state.prototype.BudgetStatePrototype
import org.hnau.upchain.core.UpchainHash

data class BudgetState(
    val prototype: BudgetStatePrototype,
    val info: BudgetInfo,
    val transactions: List<KeyValue<Transaction.Id, TransactionInfo>>,
    val categories: List<CategoryInfo>,
    val accounts: List<AccountInfo>,
) {
    val hash: UpchainHash?
        get() = prototype.hash

    val visibleAccounts: List<AccountInfo>
            by lazy { accounts.filter(AccountInfo::visible) }
}