package org.hnau.pinfin.model.utils

import arrow.core.NonEmptyList
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo


fun Transaction.Type.Entry.amount(
    currency: Currency,
): Amount = records
    .map(Record::amount)
    .amount(currency)

fun TransactionInfo.Type.Entry.amount(
    currency: Currency,
): Amount = records
    .map(TransactionInfo.Type.Entry.Record::amount)
    .amount(currency)

fun TransactionInfo.amount(
    currency: Currency,
): Amount = when (val type = type) {
    is TransactionInfo.Type.Entry -> type.amount(currency)
    is TransactionInfo.Type.Transfer -> type.amount.toAmount(currency.scale)
}

private fun NonEmptyList<AmountExpression>.amount(
    currency: Currency,
): Amount = tail.fold(
    initial = head.toAmount(currency.scale),
) { acc, record ->
    acc + record.toAmount(currency.scale)
}