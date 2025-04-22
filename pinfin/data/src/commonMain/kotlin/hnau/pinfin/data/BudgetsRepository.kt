package hnau.pinfin.data

import hnau.common.kotlin.coroutines.mapReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.data.dto.BudgetId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class BudgetsRepository(
    private val scope: CoroutineScope,
    initialBudgets: List<BudgetId>,
    private val getBudgetInfo: suspend (id: BudgetId) -> BudgetInfo,
) {

    private val _budgets: MutableStateFlow<List<BudgetId>> =
        initialBudgets.toMutableStateFlowAsInitial()

    private val infos: StateFlow<Map<BudgetId, Deferred<BudgetInfo>>> = _budgets
        .mapReusable(
            scope = scope,
            buildState = { ids ->
                ids.associateWith { id ->
                    getOrPutItem(
                        key = id,
                    ) { budgetScope ->
                        budgetScope.async { getBudgetInfo(id) }
                    }
                }
            }
        )

    val budgets: StateFlow<List<BudgetId>> = infos.mapState(scope) { infoByIds ->
        infoByIds.keys.toList()
    }

    suspend operator fun get(
        id: BudgetId,
    ): BudgetInfo = infos
        .value
        .getValue(id)
        .await()

    fun createNewBudget() {
        val info = BudgetId.new()
        _budgets.update { it + info }
    }

    fun interface Factory {

        suspend fun createBudgetsRepository(
            scope: CoroutineScope,
        ): BudgetsRepository

        companion object
    }
}