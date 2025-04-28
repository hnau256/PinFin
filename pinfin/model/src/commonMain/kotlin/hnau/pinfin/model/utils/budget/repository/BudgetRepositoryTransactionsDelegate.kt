package hnau.pinfin.model.utils.budget.repository

import hnau.common.kotlin.coroutines.mapStateLite
import hnau.pinfin.data.Transaction
import hnau.pinfin.data.UpdateType
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import kotlinx.coroutines.flow.StateFlow

class BudgetRepositoryTransactionsDelegate(
    state: StateFlow<BudgetState>,
    private val addUpdate: suspend (UpdateType) -> Unit,
) {

    val list: StateFlow<List<TransactionInfo>> =
        state.mapStateLite(BudgetState::transactions)

    suspend fun addOrUpdate(
        id: Transaction.Id?,
        transaction: Transaction,
    ) {
        addUpdate(
            UpdateType.Transaction(
                id = id ?: Transaction.Id.new(),
                transaction = transaction,
            )
        )
    }

    suspend fun remove(
        id: Transaction.Id,
    ) {
        addUpdate(
            UpdateType.RemoveTransaction(
                id = id,
            )
        )
    }

}