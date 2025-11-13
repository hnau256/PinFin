package hnau.pinfin.model.utils.budget.repository

import hnau.pinfin.data.CategoryConfig
import hnau.pinfin.data.CategoryId
import hnau.pinfin.data.UpdateType
import hnau.pinfin.model.utils.budget.state.BudgetState
import kotlinx.coroutines.flow.StateFlow

class BudgetRepositoryCategoriesDelegate(
    state: StateFlow<BudgetState>,
    private val addUpdate: suspend (UpdateType) -> Unit,
) {

    suspend fun addConfig(
        id: CategoryId,
        config: CategoryConfig,
    ) {
        addUpdate(
            UpdateType.CategoryConfig(
                id = id,
                config = config,
            )
        )
    }
}