package org.hnau.pinfin.model.utils.budget.state

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hnau.pinfin.data.AccountConfig
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.CategoryConfig
import org.hnau.pinfin.data.Comment
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.data.foldRaw
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
                val newInfo = info.copy(
                    title = info.title.optimize() + " copy",
                )
                listOf(
                    UpdateType.Config(
                        config = newInfo - defaultInfo,
                    )
                )
            },

        accounts.mapNotNull { (id, accountInfo) ->
            val defaultAccountInfo = AccountInfo.createDefault(
                id = id,
                amount = accountInfo.amount,
            )
            val delta = accountInfo - defaultAccountInfo
            delta
                .takeIf { it != AccountConfig.empty }
                ?.let { accountConfig ->
                    UpdateType.AccountConfig(
                        id = id,
                        config = accountConfig.copy(
                            title = accountConfig.title?.optimize(),
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
                            title = categoryConfig.title?.optimize(),
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
    comment = comment.optimize(),
    type = type.foldRaw(
        ifEntry = { variant ->
            variant.copy(
                records = variant.records.map { record ->
                    record.copy(
                        comment = record.comment.optimize(),
                    )
                },
            )
        },
        ifTransfer = { variant -> variant },
    ),
)

private fun String.optimize(): String = trim()

private fun Comment.optimize(): Comment = Comment(
    text = text.optimize(),
)