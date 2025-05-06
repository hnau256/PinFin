package hnau.pinfin.model.utils

import hnau.common.kotlin.coroutines.mapState
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.BudgetInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

fun BudgetRepository.toBudgetInfoStateFlow(
    scope: CoroutineScope,
): StateFlow<BudgetInfo> = state.mapState(scope) { state ->
    state.info
}