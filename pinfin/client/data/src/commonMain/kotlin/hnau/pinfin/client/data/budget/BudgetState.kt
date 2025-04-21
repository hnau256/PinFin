package hnau.pinfin.client.data.budget

data class BudgetState(
    val transactions: List<TransactionInfo>,
    val categories: List<CategoryInfo>,
    val accounts: List<AccountInfo>,
) {

    companion object
}

