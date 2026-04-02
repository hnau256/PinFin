package org.hnau.pinfin.model.utils.budget.storage

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository

suspend fun BudgetsStorage.createNewBudgetIfNotExistsAndGet(
    id: BudgetId,
): BudgetRepository {
    createNewBudgetIfNotExists(id)
    return list
        .mapNotNull { currentList ->
            currentList.firstOrNull { it.key == id }
        }
        .first()
        .value
}