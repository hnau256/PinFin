package hnau.pinfin.model.utils.budget.storage

import hnau.pinfin.data.BudgetId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow

interface BudgetsStorage {

    val list: StateFlow<List<Pair<BudgetId, Deferred<UpchainStorage>>>>

    suspend fun createNewBudget(
        id: BudgetId,
    )

    interface Factory {

        suspend fun createBudgetsStorage(
            scope: CoroutineScope,
        ): BudgetsStorage

        companion object
    }
}