package hnau.pinfin.data.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface BudgetsStorage {

    interface Factory {

        suspend fun createBudgetsStorage(
            scope: CoroutineScope,
        ): BudgetsStorage
    }

    val list: StateFlow<List<BudgetStorage>>
}