package org.hnau.pinfin.model.utils.budget.state

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hnau.pinfin.data.AccountConfig
import org.hnau.pinfin.data.CategoryConfig
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.model.transaction.utils.toTransactionType

suspend fun BudgetState.toOptimizedUpdates(): List<UpdateType> = withContext(Dispatchers.Default) {
    listOf(
        listOf(
            UpdateType.Config(
                config = info.toConfig(),
            ),
        ),

        accounts.mapNotNull { (id, accountInfo) ->
            val defaultAccountInfo = AccountInfo.createDefault(
                id = id,
                amount = accountInfo.amount,
            )
            val delta = accountInfo - defaultAccountInfo
            delta
                .takeIf { it != AccountConfig.empty }
                ?.let {
                    UpdateType.AccountConfig(
                        id = id,
                        config = it,
                    )
                }
        },

        categories.mapNotNull { (id, categoryInfo) ->
            val defaultCategoryInfo = CategoryInfo.createDefault(
                id = id,
            )
            val delta = categoryInfo - defaultCategoryInfo
            delta
                .takeIf { it != CategoryConfig.empty }
                ?.let {
                    UpdateType.CategoryConfig(
                        id = id,
                        config = it,
                    )
                }
        },

        transactions.map { (id, transactionInfo) ->
            UpdateType.Transaction(
                id = id,
                transaction = Transaction(
                    timestamp = transactionInfo.timestamp,
                    comment = transactionInfo.comment,
                    type = transactionInfo.type.toTransactionType(),
                ),
            )
        },
    ).flatten()
}