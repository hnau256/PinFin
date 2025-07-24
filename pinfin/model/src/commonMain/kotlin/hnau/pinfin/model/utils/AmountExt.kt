package hnau.pinfin.model.utils

import hnau.pinfin.data.Amount
import hnau.pinfin.data.Transaction
import hnau.pinfin.model.utils.budget.state.TransactionInfo

val Transaction.Type.Entry.amount: Amount
    get() = records.tail.fold(
        initial = records.head.amount,
    ) { acc, record ->
        acc + record.amount
    }

val TransactionInfo.Type.Entry.amount: Amount
    get() = records.tail.fold(
        initial = records.head.amount,
    ) { acc, record ->
        acc + record.amount
    }

val TransactionInfo.amount: Amount
    get() = when (val type = type) {
        is TransactionInfo.Type.Entry -> type.amount
        is TransactionInfo.Type.Transfer -> type.amount
    }