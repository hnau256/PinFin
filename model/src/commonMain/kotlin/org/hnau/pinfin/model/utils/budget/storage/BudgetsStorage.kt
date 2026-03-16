package org.hnau.pinfin.model.utils.budget.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository

interface BudgetsStorage {

    val list: StateFlow<List<Pair<BudgetId, BudgetRepository>>>

    suspend fun createNewBudgetIfNotExists(
        id: BudgetId,
    )

    fun interface Factory {

        suspend fun createBudgetsStorage(
            scope: CoroutineScope,
        ): BudgetsStorage

        companion object
    }
}