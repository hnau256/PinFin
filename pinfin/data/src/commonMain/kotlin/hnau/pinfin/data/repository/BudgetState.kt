package hnau.pinfin.data.repository

data class BudgetState(
    val transactions: List<TransactionInfo>,
    val categories: List<CategoryInfo>,
    val accounts: List<AccountInfo>,
) {

    companion object
}

