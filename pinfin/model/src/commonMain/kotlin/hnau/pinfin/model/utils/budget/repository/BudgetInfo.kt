package hnau.pinfin.model.utils.budget.repository

import hnau.pinfin.model.utils.budget.storage.UpchainStorage

data class BudgetInfo(
    val upchainStorage: UpchainStorage,
    val repository: BudgetRepository,
)