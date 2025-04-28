package hnau.pinfin.model.utils.budget.storage

import hnau.pinfin.model.utils.budget.repository.BudgetRepository

data class BudgetStorage(
    val upchainStorage: UpchainStorage,
    val budgetRepository: BudgetRepository,
)