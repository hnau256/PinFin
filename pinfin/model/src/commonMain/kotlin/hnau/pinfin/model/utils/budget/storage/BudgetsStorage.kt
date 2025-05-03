package hnau.pinfin.model.utils.budget.storage

import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

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