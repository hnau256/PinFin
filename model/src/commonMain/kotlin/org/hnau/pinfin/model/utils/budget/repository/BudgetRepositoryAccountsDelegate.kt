package org.hnau.pinfin.model.utils.budget.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.pinfin.data.AccountConfig
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.BudgetState

class BudgetRepositoryAccountsDelegate(
    private val scope: CoroutineScope,
    private val state: StateFlow<BudgetState>,
    private val addUpdate: suspend (UpdateType) -> Unit,
) {

    private val lists: MutableMap<Boolean, StateFlow<List<AccountInfo>>> = HashMap()

    suspend fun addConfig(
        id: AccountId,
        config: AccountConfig,
    ) {
        addUpdate(
            UpdateType.AccountConfig(
                id = id,
                config = config,
            )
        )
    }

    fun getList(
        includeInvisible: Boolean,
    ): StateFlow<List<AccountInfo>> = lists.getOrPut(
        key = includeInvisible,
    ) {
        state.mapState(scope) { budgetState ->
            budgetState
                .accounts
                .filter { includeInvisible || it.visible }
        }
    }
}