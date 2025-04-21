package hnau.pinfin.client.data.budget

import hnau.common.kotlin.coroutines.mapStateLite
import hnau.pinfin.scheme.Update
import kotlinx.coroutines.flow.StateFlow

class BudgetRepositoryAccountsDelegate(
    state: StateFlow<BudgetState>,
    private val addUpdate: suspend (Update) -> Unit,
) {

    val list: StateFlow<List<AccountInfo>> =
        state.mapStateLite(BudgetState::accounts)
}