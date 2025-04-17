package hnau.pinfin.client.data.budget

import hnau.pinfin.client.data.UpdateRepository
import hnau.pinfin.scheme.Transaction
import hnau.pinfin.scheme.Update
import kotlinx.coroutines.CoroutineScope

class BudgetRepository(
    scope: CoroutineScope,
    initialTransactions: Map<Transaction.Id, Transaction>,
    private val addUpdate: suspend (Update) -> Unit,
) {

    val transaction = TransactionRepository(
        scope = scope,
        initialTransactions,
        addUpdate = addUpdate,
    )

    val account = AccountRepository(
        scope = scope,
        transactions = transaction,
    )

    val category = CategoryRepository(
        scope = scope,
        transactions = transaction,
    )

    companion object {

        suspend fun create(
            scope: CoroutineScope,
            updateRepository: UpdateRepository,
        ): BudgetRepository {
            val transactions = HashMap<Transaction.Id, Transaction>()
            updateRepository.useUpdates { updates ->
                updates.forEach { update ->
                    when (update) {
                        is Update.Transaction -> transactions.set(
                            key = update.id,
                            value = update.transaction,
                        )
                        is Update.RemoveTransaction -> transactions.remove(
                            key = update.id,
                        )
                    }
                }
            }
            return BudgetRepository(
                scope = scope,
                initialTransactions = transactions,
                addUpdate = updateRepository::addUpdate,
            )
        }
    }
}