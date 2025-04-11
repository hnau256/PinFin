package hnau.pinfin.client.data.budget

import hnau.common.kotlin.Loadable
import hnau.pinfin.scheme.AccountId
import hnau.pinfin.scheme.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class AccountRepository(
    scope: CoroutineScope,
    transactions: TransactionRepository,
) : AccountInfoResolver {

    val list: StateFlow<Loadable<List<AccountId>>> = transactions
        .map
        .map { transactions ->
            withContext(Dispatchers.Default) {
                val accountsSet = buildSet<AccountId> {
                    transactions.forEach { transaction ->
                        when (val type = transaction.value.type) {
                            is Transaction.Type.Entry -> add(type.account)
                            is Transaction.Type.Transfer -> {
                                add(type.from)
                                add(type.to)
                            }
                        }
                    }
                }
                Loadable.Ready(accountsSet.toList())
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = Loadable.Loading,
        )

    override fun get(
        accountId: AccountId,
    ): AccountInfo = AccountInfo(
        title = accountId.id, //TODO custom title
    )
}