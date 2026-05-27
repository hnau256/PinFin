package org.hnau.pinfin.model.transaction.utils

import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo

fun TransactionInfo.Type.toTransactionType(): Transaction.Type = when (this) {
    is TransactionInfo.Type.Transfer -> toTransactionTransferType()
    is TransactionInfo.Type.Entry -> toTransactionEntryType()
}

fun TransactionInfo.Type.Transfer.toTransactionTransferType(): Transaction.Type.Transfer =
    Transaction.Type.Transfer(
        from = from.key,
        to = to.key,
        amount = amount,
    )

fun TransactionInfo.Type.Entry.toTransactionEntryType(): Transaction.Type.Entry =
    Transaction.Type.Entry(
        account = idWithAccount.key,
        records = records.map { record ->
            Record(
                category = record.idWithCategory.key,
                amount = record.amount,
                comment = record.comment,
            )
        }
    )