package org.hnau.pinfin.model.utils.budget.repository

import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.data.AccountConfig
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.model.utils.budget.state.BudgetState

class BudgetRepositoryAccountsDelegate(
    private val state: StateFlow<BudgetState>,
    private val addUpdate: suspend (UpdateType) -> Unit,
) {

    suspend fun addConfig(
        id: AccountId,
        config: AccountConfig,
    ) {
        val infoOrNull = state.value.accounts.find { it.key == id }?.value
        val delta = infoOrNull.foldNullable(
            ifNull = { config },
            ifNotNull = { info ->
                val newInfo = info + config
                newInfo - info
            }
        )
        if (delta == AccountConfig.empty) {
            return
        }
        addUpdate(
            UpdateType.AccountConfig(
                id = id,
                config = delta,
            )
        )
    }
}