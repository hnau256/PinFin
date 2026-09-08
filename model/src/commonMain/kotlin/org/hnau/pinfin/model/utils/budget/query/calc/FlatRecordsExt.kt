package org.hnau.pinfin.model.utils.budget.query.calc

import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.fold
import org.hnau.pinfin.model.utils.budget.query.FlatRecord
import org.hnau.pinfin.model.utils.budget.query.RecordDirection
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.budget.state.fold

internal fun AmountDirection.toRecordDirection(): RecordDirection = fold(
    ifCredit = { RecordDirection.Credit },
    ifDebit = { RecordDirection.Debit },
)

/**
 * Flattens every transaction into [FlatRecord]s. A transfer becomes two records without a
 * category: `from` with direction [RecordDirection.Debit], `to` with [RecordDirection.Credit].
 */
internal fun BudgetState.flatRecords(
    newestFirst: Boolean = false,
): List<FlatRecord> {
    val scale = info.currency.scale
    val orderedTransactions = if (newestFirst) transactions.asReversed() else transactions
    return orderedTransactions.flatMap { (id, transaction) ->
        val date = transaction.timestamp
        transaction.type.fold(
            ifEntry = { idWithAccount, records ->
                records.map { record ->
                    FlatRecord(
                        transactionId = id,
                        date = date,
                        account = idWithAccount.key,
                        category = record.idWithCategory.key,
                        direction = record.idWithCategory.key.direction.toRecordDirection(),
                        amount = record.amount.toAmount(scale),
                        comment = record.comment.text.takeIf(String::isNotBlank),
                        transferCounterpartAccount = null,
                    )
                }
            },
            ifTransfer = { from, to, amountExpression ->
                val amount = amountExpression.toAmount(scale)
                listOf(
                    FlatRecord(
                        transactionId = id,
                        date = date,
                        account = from.key,
                        category = null,
                        direction = RecordDirection.Debit,
                        amount = amount,
                        comment = null,
                        transferCounterpartAccount = to.key,
                    ),
                    FlatRecord(
                        transactionId = id,
                        date = date,
                        account = to.key,
                        category = null,
                        direction = RecordDirection.Credit,
                        amount = amount,
                        comment = null,
                        transferCounterpartAccount = from.key,
                    ),
                )
            },
        )
    }
}
