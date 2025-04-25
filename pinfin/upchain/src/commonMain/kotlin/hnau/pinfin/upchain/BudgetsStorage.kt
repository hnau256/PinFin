package hnau.pinfin.upchain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface BudgetsStorage {

    interface Factory {

        suspend fun createBudgetsStorage(
            scope: CoroutineScope,
        ): BudgetsStorage
    }

    val list: StateFlow<List<Pair<BudgetId, BudgetUpchain>>>

    fun createNewBudget()
}