package org.hnau.pinfin.model.utils.budget.repository

import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import kotlinx.coroutines.flow.StateFlow

class BudgetRepositoryTransactionsDelegate(
    state: StateFlow<BudgetState>,
    private val addUpdate: suspend (UpdateType) -> Unit,
) {

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