package hnau.pinfin.model.utils.budget.repository

import hnau.common.kotlin.coroutines.mapStateLite
import hnau.pinfin.data.UpdateType
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import kotlinx.coroutines.flow.StateFlow

class BudgetRepositoryCategoriesDelegate(
    state: StateFlow<BudgetState>,
    private val addUpdate: suspend (UpdateType) -> Unit,
) {

    val list: StateFlow<List<CategoryInfo>> =
        state.mapStateLite(BudgetState::categories)
}