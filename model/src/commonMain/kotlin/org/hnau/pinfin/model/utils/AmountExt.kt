package org.hnau.pinfin.model.utils

import arrow.core.NonEmptyList
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.directionedAmount
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.pinfin.data.plus
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo


fun Transaction.Type.Entry<AccountId, CategoryId, *>.amount(
    currency: Currency,
): KeyValue<AmountDirection, Amount> = records
    .records
    .map(Record<CategoryId>::directionedAmount)
    .amount(currency)

val Record<KeyValue<CategoryId, CategoryInfo>>.resolvedDirectionedAmount: KeyValue<AmountDirection, AmountExpression>
    get() = KeyValue(
        key = category.key.direction,
        value = amount,
    )

private fun NonEmptyList<KeyValue<AmountDirection, AmountExpression>>.amount(
    currency: Currency,
): KeyValue<AmountDirection, Amount> = tail.fold(
    initial = head.map { it.toAmount(currency.scale) },
) { acc, record ->
    acc + record.map { it.toAmount(currency.scale) }
}