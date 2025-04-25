package hnau.pinfin.repository

import hnau.common.kotlin.coroutines.mapStateLite
import hnau.pinfin.repository.dto.Transaction
import hnau.pinfin.repository.dto.UpdateType
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