package org.hnau.pinfin.model.utils.budget.repository

import org.hnau.pinfin.data.CategoryConfig
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.model.utils.budget.state.BudgetState
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