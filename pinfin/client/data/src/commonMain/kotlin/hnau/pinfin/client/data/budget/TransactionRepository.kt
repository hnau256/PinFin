package hnau.pinfin.client.data.budget

import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.scheme.Transaction
import hnau.pinfin.scheme.Update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class TransactionRepository(
    scope: CoroutineScope,
    initialTransactions: Map<Transaction.Id, Transaction>,
    private val addUpdate: suspend (Update) -> Unit,
) {

    val map: MutableStateFlow<Map<Transaction.Id, Transaction>> =
        initialTransactions.toMutableStateFlowAsInitial()

    val list: StateFlow<Loadable<List<Pair<Transaction.Id, Transaction>>>> =
        map
            .map { transactions ->
                withContext(Dispatchers.Default) {
                    transactions
                        .entries
                        .map { (id, transaction) ->
                            id to transaction
                        }
                        .sortedBy { (_, transaction) ->
                            transaction.timestamp
                        }
                        .let { Loadable.Ready(it) }
                }
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = Loadable.Loading,
            )

    suspend fun addOrUpdate(
        id: Transaction.Id?,
        transaction: Transaction,
    ) {
        val actualId = id ?: Transaction.Id.new()
        addUpdate(
            Update.Transaction(
                id = actualId,
                transaction = transaction,
            )
        )
        map.update {
            it + (actualId to transaction)
        }
    }
}