package hnau.pinfin.model.utils.budget.repository

import hnau.common.kotlin.coroutines.mapState
import hnau.pinfin.data.AccountConfig
import hnau.pinfin.data.AccountId
import hnau.pinfin.data.UpdateType
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.budget.state.BudgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

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