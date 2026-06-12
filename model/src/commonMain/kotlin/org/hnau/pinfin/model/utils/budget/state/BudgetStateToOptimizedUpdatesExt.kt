package org.hnau.pinfin.model.utils.budget.state

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hnau.pinfin.data.AccountConfig
import org.hnau.pinfin.data.BudgetConfig
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.CategoryConfig
import org.hnau.pinfin.data.Comment
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.model.transaction.utils.toTransactionType

suspend fun BudgetState.toOptimizedUpdates(
    sourceId: BudgetId,
): List<UpdateType> = withContext(Dispatchers.Default) {
    listOf(
        BudgetInfo
            .create(
                id = sourceId,
                config = null,
            )
            .let { defaultInfo ->
                val config = info - defaultInfo
                config
                    .takeIf { it != BudgetConfig.empty }
                    ?.let { budgetConfig ->
                        listOf(
                            UpdateType.Config(
                                config = budgetConfig.copy(
                                    title = budgetConfig.title?.trim()
                                )
                            )
                        )
                    }
                    ?: emptyList()
            },

        accounts.mapNotNull { (id, accountInfo) ->
            val defaultAccountInfo = AccountInfo.createDefault(
                id = id,
                amount = accountInfo.amount,
            )
            val delta = accountInfo - defaultAccountInfo
            delta
                .takeIf { it != AccountConfig.empty }
                ?.let { acountConfig ->
                    UpdateType.AccountConfig(
                        id = id,
                        config = acountConfig.copy(
                            title = acountConfig.title?.trim(),
                        ),
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
                ?.let { categoryConfig ->
                    UpdateType.CategoryConfig(
                        id = id,
                        config = categoryConfig.copy(
                            title = categoryConfig.title?.trim(),
                        ),
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
                ).trimStrings(),
            )
        },
    ).flatten()
}

private fun Transaction.trimStrings(): Transaction = copy(
    comment = Comment(comment.text.trim()),
    type = when (val type = type) {
        is Transaction.Type.Entry -> type.copy(
            records = type.records.map { record ->
                record.copy(
                    comment = Comment(record.comment.text.trim()),
                )
            },
        )

        is Transaction.Type.Transfer -> type
    },
)