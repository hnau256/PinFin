package hnau.pinfin.data.budget

import hnau.common.kotlin.coroutines.mapStateLite
import hnau.pinfin.data.dto.Update
import kotlinx.coroutines.flow.StateFlow

class BudgetRepositoryCategoriesDelegate(
    state: StateFlow<BudgetState>,
    private val addUpdate: suspend (Update) -> Unit,
) {

    val list: StateFlow<List<CategoryInfo>> =
        state.mapStateLite(BudgetState::categories)
}