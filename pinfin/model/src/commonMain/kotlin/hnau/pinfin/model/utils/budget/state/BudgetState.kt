package hnau.pinfin.model.utils.budget.state

data class BudgetState(
    val info: BudgetInfo,
    val transactions: List<TransactionInfo>,
    val categories: List<CategoryInfo>,
    val accounts: List<AccountInfo>,
)