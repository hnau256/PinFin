package hnau.pinfin.client.data.budget

import hnau.common.kotlin.Loadable
import hnau.pinfin.scheme.CategoryId
import hnau.pinfin.scheme.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class CategoryRepository(
    scope: CoroutineScope,
    transactions: TransactionRepository,
) : CategoryInfoResolver {

    val list: StateFlow<Loadable<List<CategoryId>>> = transactions
        .map
        .map { transactions ->
            withContext(Dispatchers.Default) {
                val categoriesSet = buildSet<CategoryId> {
                    transactions.forEach { transaction ->
                        when (val type = transaction.value.type) {
                            is Transaction.Type.Entry -> addAll(type.records.map { it.category })
                            is Transaction.Type.Transfer -> Unit
                        }
                    }
                }
                Loadable.Ready(categoriesSet.toList())
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = Loadable.Loading,
        )

    override fun get(
        categoryId: CategoryId,
    ): CategoryInfo = CategoryInfo(
        title = categoryId.titleBasedOnId, //TODO custom title
    )
}