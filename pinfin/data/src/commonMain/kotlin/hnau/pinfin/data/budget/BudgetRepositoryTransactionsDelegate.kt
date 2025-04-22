package hnau.pinfin.data.budget

import hnau.common.kotlin.coroutines.mapStateLite
import hnau.pinfin.data.dto.Transaction
import hnau.pinfin.data.dto.Update
import kotlinx.coroutines.flow.StateFlow

class BudgetRepositoryTransactionsDelegate(
    state: StateFlow<BudgetState>,
    private val addUpdate: suspend (Update) -> Unit,
) {

    val list: StateFlow<List<TransactionInfo>> =
        state.mapStateLite(BudgetState::transactions)

    suspend fun addOrUpdate(
        id: Transaction.Id?,
        transaction: Transaction,
    ) {
        addUpdate(
            Update.Transaction(
                id = id ?: Transaction.Id.new(),
                transaction = transaction,
            )
        )
    }

    suspend fun remove(
        id: Transaction.Id,
    ) {
        addUpdate(
            Update.RemoveTransaction(
                id = id,
            )
        )
    }

}