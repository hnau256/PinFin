package hnau.pinfin.model.utils.budget.repository

import hnau.common.kotlin.coroutines.mapListReusable
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.StateFlow

class BudgetsRepository(
    scope: CoroutineScope,
    private val budgetsStorage: BudgetsStorage,
) {

    val list: StateFlow<List<Pair<BudgetId, Deferred<BudgetRepository>>>> = budgetsStorage
        .list
        .mapListReusable(
            scope = scope,
            extractKey = { idWithBudget -> idWithBudget.first },
            transform = { budgetScope, (id, deferredUpchainStorage) ->
                val deferredInfo = budgetScope.async {
                    val upchainStorage = deferredUpchainStorage.await()
                    BudgetRepository(
                        scope = budgetScope,
                        upchainStorage = upchainStorage,
                    )
                }
                id to deferredInfo
            }
        )

    suspend fun createNewBudgetIfNotExists(
        id: BudgetId,
    ) {
        budgetsStorage.createNewBudgetIfNotExists(
            id = id,
        )
    }
}