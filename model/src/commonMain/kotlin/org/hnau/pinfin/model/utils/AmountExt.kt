package org.hnau.pinfin.model.utils

import arrow.core.NonEmptyList
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.directionedAmount
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.pinfin.data.plus
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo


fun Transaction.Type.Entry.amount(
    currency: Currency,
): KeyValue<AmountDirection, Amount> = records
    .map(Record::directionedAmount)
    .amount(currency)

fun TransactionInfo.Type.Entry.amount(
    currency: Currency,
): KeyValue<AmountDirection, Amount> = records
    .map(TransactionInfo.Type.Entry.Record::directionedAmount)
    .amount(currency)

private fun NonEmptyList<KeyValue<AmountDirection, AmountExpression>>.amount(
    currency: Currency,
): KeyValue<AmountDirection, Amount> = tail.fold(
    initial = head.map { it.toAmount(currency.scale) },
) { acc, record ->
    acc + record.map { it.toAmount(currency.scale) }
}