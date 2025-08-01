package hnau.pinfin.model.transaction_old

import hnau.pinfin.data.Record
import hnau.pinfin.data.Transaction
import hnau.pinfin.model.utils.budget.state.TransactionInfo

fun TransactionInfo.toTransaction(): Pair<Transaction.Id, Transaction> {
    val transaction = Transaction(
        timestamp = timestamp,
        comment = comment,
        type = type.toTransactionType(),
    )
    return id to transaction
}

fun TransactionInfo.Type.toTransactionType(): Transaction.Type = when (this) {
    is TransactionInfo.Type.Transfer -> toTransactionTransferType()
    is TransactionInfo.Type.Entry -> toTransactionEntryType()
}

fun TransactionInfo.Type.Transfer.toTransactionTransferType(): Transaction.Type.Transfer =
    Transaction.Type.Transfer(
        from = from.id,
        to = to.id,
        amount = amount,
    )

fun TransactionInfo.Type.Entry.toTransactionEntryType(): Transaction.Type.Entry =
    Transaction.Type.Entry(
        account = account.id,
        records = records.map { record ->
            Record(
                category = record.category.id,
                amount = record.amount,
                comment = record.comment,
            )
        }
    )