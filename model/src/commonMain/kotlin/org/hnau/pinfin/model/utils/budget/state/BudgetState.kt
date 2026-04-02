package org.hnau.pinfin.model.utils.budget.state

import org.hnau.upchain.core.UpchainHash

data class BudgetState(
    val hash: UpchainHash?,
    val info: BudgetInfo,
    val transactions: List<TransactionInfo>,
    val categories: List<CategoryInfo>,
    val accounts: List<AccountInfo>,
) {

    val visibleAccounts: List<AccountInfo>
            by lazy { accounts.filter(AccountInfo::visible) }
}