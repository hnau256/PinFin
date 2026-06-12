package org.hnau.pinfin.model.utils.budget.state

import org.hnau.pinfin.data.Comment
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.UpdateType

fun UpdateType.trimStrings(): UpdateType = when (this) {
    is UpdateType.Config -> copy(
        config = config.copy(
            title = config.title?.trim(),
        ),
    )
    is UpdateType.AccountConfig -> copy(
        config = config.copy(
            title = config.title?.trim(),
        ),
    )
    is UpdateType.CategoryConfig -> copy(
        config = config.copy(
            title = config.title?.trim(),
        ),
    )
    is UpdateType.Transaction -> copy(
        transaction = transaction.copy(
            comment = Comment(transaction.comment.text.trim()),
            type = transaction.type.trimStrings(),
        ),
    )
    is UpdateType.RemoveTransaction -> this
}

private fun Transaction.Type.trimStrings(): Transaction.Type = when (this) {
    is Transaction.Type.Entry -> copy(
        records = records.map { record ->
            record.copy(
                comment = Comment(record.comment.text.trim()),
            )
        },
    )
    is Transaction.Type.Transfer -> this
}
