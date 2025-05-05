package hnau.pinfin.model.utils.budget.storage

import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

suspend fun BudgetsStorage.createNewBudgetIfNotExistsAndGet(
    id: BudgetId,
): BudgetRepository {
    createNewBudgetIfNotExists(id)
    return list
        .mapNotNull { it.firstOrNull { it.first == id } }
        .first()
        .second
}