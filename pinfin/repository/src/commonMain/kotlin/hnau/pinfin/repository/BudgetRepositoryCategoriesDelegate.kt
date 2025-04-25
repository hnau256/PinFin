package hnau.pinfin.repository

import hnau.common.kotlin.coroutines.mapStateLite
import hnau.pinfin.data.Update
import kotlinx.coroutines.flow.StateFlow

class BudgetRepositoryCategoriesDelegate(
    state: StateFlow<BudgetState>,
    private val addUpdate: suspend (Update) -> Unit,
) {

    val list: StateFlow<List<CategoryInfo>> =
        state.mapStateLite(BudgetState::categories)
}