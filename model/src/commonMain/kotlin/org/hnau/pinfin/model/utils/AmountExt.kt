package org.hnau.pinfin.model.utils

import arrow.core.NonEmptyList
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.pinfin.data.plus
import org.hnau.pinfin.data.records.FilteredRecords
import org.hnau.pinfin.data.records.Records
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import kotlin.jvm.JvmName


@JvmName("unresolvedTotalAmount")
fun Records<CategoryId>.totalAmount(
    currency: Currency,
): KeyValue<AmountDirection, Amount> = records
    .map { record -> KeyValue(record.category.direction, record.amount) }
    .amount(currency)

fun Records<KeyValue<CategoryId, CategoryInfo>>.totalAmount(
    currency: Currency,
): KeyValue<AmountDirection, Amount> = records
    .map { record -> KeyValue(record.resolvedDirection, record.amount) }
    .amount(currency)

fun FilteredRecords<KeyValue<CategoryId, CategoryInfo>>.filteredAmount(
    currency: Currency,
): KeyValue<AmountDirection, Amount> = main
    .map { record -> KeyValue(record.resolvedDirection, record.amount) }
    .amount(currency)

val Record<KeyValue<CategoryId, CategoryInfo>>.resolvedDirection: AmountDirection
    get() = category.key.direction

private fun NonEmptyList<KeyValue<AmountDirection, AmountExpression>>.amount(
    currency: Currency,
): KeyValue<AmountDirection, Amount> = tail.fold(
    initial = head.map { it.toAmount(currency.scale) },
) { acc, record ->
    acc + record.map { it.toAmount(currency.scale) }
}