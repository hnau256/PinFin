package hnau.pinfin.model.utils.budget.repository

import hnau.common.kotlin.coroutines.mapStateLite
import hnau.pinfin.data.UpdateType
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.utils.budget.storage.UpchainStorage
import kotlinx.coroutines.flow.StateFlow

class BudgetRepositoryAccountsDelegate(
    state: StateFlow<BudgetState>,
    private val addUpdate: suspend (UpdateType) -> Unit,
) {

    val list: StateFlow<List<AccountInfo>> =
        state.mapStateLite(BudgetState::accounts)
}