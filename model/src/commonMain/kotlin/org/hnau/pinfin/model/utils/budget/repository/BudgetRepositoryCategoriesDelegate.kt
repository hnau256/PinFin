package org.hnau.pinfin.model.utils.budget.repository

import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.data.CategoryConfig
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.model.utils.budget.state.BudgetState

class BudgetRepositoryCategoriesDelegate(
    private val state: StateFlow<BudgetState>,
    private val addUpdate: suspend (UpdateType) -> Unit,
) {

    suspend fun addConfig(
        id: CategoryId,
        config: CategoryConfig,
    ) {
        val infoOrNull = state.value.categories.find { it.key == id }?.value
        val delta = infoOrNull.foldNullable(
            ifNull = { config },
            ifNotNull = { info ->
                val newInfo = info + config
                newInfo - info
            }
        )
        if (delta == CategoryConfig.empty) {
            return
        }
        addUpdate(
            UpdateType.CategoryConfig(
                id = id,
                config = delta,
            )
        )
    }
}