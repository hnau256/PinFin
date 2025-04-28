package hnau.pinfin.model.utils.budget.state

data class BudgetState(
    val transactions: List<TransactionInfo>,
    val categories: List<CategoryInfo>,
    val accounts: List<AccountInfo>,
)