package org.hnau.pinfin.model.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.BudgetInfo

fun BudgetRepository.toBudgetInfoStateFlow(
    scope: CoroutineScope,
): StateFlow<BudgetInfo> = state.mapState(scope) { state ->
    state.info
}